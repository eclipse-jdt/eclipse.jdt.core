/*******************************************************************************
 * Copyright (c) 2016, 2021 IBM Corporation.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class GenericsRegressionTest_9 extends AbstractRegressionTest9 {

static {
//	TESTS_NAMES = new String[] { "testBug551913_001", "testBug551913_002" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public GenericsRegressionTest_9(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_9);
}

// vanilla test case
public void testBug488663_001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public Y<String> bar() {
						Y<String> y = new Y<>() {
							@Override
							public void foo(String s) {
								this.s = s;
					 		}
						};
						return y;
					}
					public static void main(String[] args) {
						Y<String> y = new X().bar();
						y.foo("Done");
						y.print();
					}
				}
				abstract class Y<T> {
					String s;
					public abstract void foo(String s);
					public void print() {
						System.out.println(this.s);
					}
				}
				""",
		},
		"Done");
}

// negative test case for diamond operator instantiation of denotable anonymous type but with parameterized method
public void testBug488663_002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public Y<String> bar() {
						Y<String> y = new Y<>() {
							@Override
							public void foo(T t) {
								this.s = t;
							}
						};
						return y;
					}
					public static void main(String[] args) {
						Y<String> y = new X().bar();
						y.foo("Done");
						y.print();
					}
				}
				abstract class Y<T> {
					T s;
					public abstract void foo(T t);
					public void print() {
						System.out.println(this.s);
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Y<String> y = new Y<>() {
				                  ^^^^^
			The type new Y<String>(){} must implement the inherited abstract method Y<String>.foo(String)
			----------
			2. ERROR in X.java (at line 5)
				public void foo(T t) {
				                ^
			T cannot be resolved to a type
			----------
			""");
}

// diamond operator instantiation of denotable anonymous types with different type params
public void testBug488663_003() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				@SuppressWarnings("unused")\s
					public static void main(String[] args) {
						Y<?> y1 = new Y<>(){};
						Y<String> y2 = new Y<>(){};
						Y<? extends String> y3 = new Y<>() {};
						Y<? super String> y4 = new Y<>() {};
					}
				}
				class Y<T> {}
				""",
		},
		"");
}

// inner classes with diamond operator and anonymous classes
public void testBug488663_004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				@SuppressWarnings("unused")\s
					public static void main(String[] args) {
						Y<?> y1 = new X().new Y<>(){};
						Y<String> y2 = new X().new Y<>(){};
						Y<? extends String> y3 = new X().new Y<>() {};
						Y<? super String> y4 = new X().new Y<>() {};
					}
				
					class Y<T> {}
				}
				""",
		},
		"");
}

// compiler error for non-denotable anonymous type with diamond operator - negative test
public void testBug488663_005() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface I {}
					interface J{}
					class Y<T extends I & J> {}
					
					public class X {
						public static void main(String[] args) {
							Y<?> y = new Y<>() {};
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					Y<?> y = new Y<>() {};
					             ^
				Type Y<I & J> inferred for Y<>, is not valid for an anonymous class with '<>'
				----------
				""");

}

//compiler error for non-denotable anonymous type with diamond operator - negative test
public void testBug488663_006() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class Y<T> {
					   Y(T x) {}
					}
					
					class X {
					  public static void main(String[] args) {
						  Y<? extends Integer> fi = null;
						  Y<?> f = new Y<>(fi){};
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					Y<?> f = new Y<>(fi){};
					             ^
				Type Y<Y<capture#1-of ? extends Integer>> inferred for Y<>, is not valid for an anonymous class with '<>'
				----------
				""");

}
// instantiate an interface using the anonymous diamond
public void testBug488663_007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					String name() {
						return this.name;
					}
					public static void main(String[] args) {
						X x = new X("Success");
						I<X> i = new I<>() {
							public String toString(X x1) {
								return x1.name();
							}
						};
						System.out.println(i.toString(x));
					}
				}
				interface I<T> {
					String toString(T t);
				}"""
		},
		"Success");
}
// anonymous diamond instantiating interface as argument to an invocation
public void testBug488663_008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					<T> void print(T o, I<T> converter) {
						System.out.println(converter.toString(o));
					}
					String name() {
						return this.name;
					}
					public static void main(String[] args) {
						X x = new X("Success");
						x.print(x, new I<>() {
							public String toString(X x1) {
								return x1.name();
							}
						});
					}
				}
				interface I<T> {
					String toString(T t);
				}"""
		},
		"Success");
}
// anonymous diamond instantiating an abstract class as argument to an invocation
public void testBug488663_009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					<T> void print(T o, I<T> converter) {
						System.out.println(converter.toString(o));
					}
					String name() {
						return this.name;
					}
					public static void main(String[] args) {
						X x = new X("Success");
						x.print(x, new Z<>() {
							public String toString(X x1) {
								return x1.name();
							}
						});
					}
				}
				interface I<T> {
					String toString(T t);
				}
				abstract class Z<T> implements I<T> {}
				"""
		},
		"Success");
}
// anonymous diamond with polytype argument
public void testBug488663_010() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					public static void main(String[] args) {
						Y<String> y = new Y<>(() -> System.out.println("Done")) {
						};
					}
				}
				interface J {
					void doSomething();
				}
				class Y<T> {
					public Y(J j) {
						j.doSomething();
					}
				}""",
		},
		"Done");
}
// anonymous diamond with polytype argument
public void testBug488663_011() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					public static void main(String[] args) {
						Y<String> y = new Y<>(Y::foo) {
						};
					}
				}
				interface J {
					void doSomething();
				}
				class Y<T> {
					public Y(J j) {
						j.doSomething();
					}
					static void foo() {
						System.out.println("Done");
					}
				}""",
		},
		"Done");
}
// Nested anonymous diamonds - TODO - confirm that this is indeed correct as per spec
public void testBug488663_012() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					String name() {
						return this.name;
					}
					public static void main(String[] args) {
						Y<String> y = new Y<>("Done", new I<>() {
								public void doSomething(String s) {
									System.out.println(s);
								}
							}){
						};
					}
				}
				interface I<T> {
					void doSomething(T t);
				}
				class Y<T> {
					public Y(T t, I<T> i) {
						i.doSomething(t);
					}
				}""",
		},
		"Done");
}
// Redundant type argument specification - TODO - confirm that this is correct
public void testBug488663_013() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					String name() {
						return this.name;
					}
					public static void main(String[] args) {
						X x = new X("Success");
						I<X> i = new I<X>() {
							public String toString(X x1) {
								return x1.name();
							}
						};
						System.out.println(i.toString(x));
					}
				}
				interface I<T> {
					String toString(T t);
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				I<X> i = new I<X>() {
				             ^
			Redundant specification of type arguments <X>
			----------
			""",
		null, true, options);
}
// All non-private methods of an anonymous class instantiated with '<>' must be treated as being annotated with @override
public void testBug488663_014() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					<T> void print(T o, I<T> converter) {
						System.out.println(converter.toString(o));
					}
					String name() {
						return this.name;
					}
					public static void main(String[] args) {
						X x = new X("asdasfd");
						x.print(x, new Z<>() {
							public String toString(String s) {
								return s;
							}
						});
					}
				}
				interface I<T> {
					String toString(T t);
				}
				class Z<T> implements I<T> {
					public String toString(T t) {
						return "";
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				public String toString(String s) {
				              ^^^^^^^^^^^^^^^^^^
			The method toString(String) of type new Z<X>(){} must override or implement a supertype method
			----------
			""");
}
// Inaccessible type inferred for anonymous diamond is an error
public void testBug488663_015() {
	this.runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test<T> {
					private static class Inner {\
						public Inner(){}
					}
					<R> void print(I<R> i) {}
					public Inner get() {
						return new Inner();
					}
				}
				""",
			"Z.java",
			"""
				class Z<T> implements I<T> {
					public Z(T t1) {}
					public String toString (T t) {
						return t.toString();
					}
				}""",
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Test<String> t = new Test<>();
						t.print(new Z<>(t.get()) {
						\t
						});
					}
				}
				interface I<T> {
					String toString();
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				t.print(new Z<>(t.get()) {
				            ^^^^^^^^^^^^
			The type Test$Inner is not visible
			----------
			""");
}
// Inaccessible type inferred for anonymous diamond is an error - interface case
public void testBug488663_016() {
	this.runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test<T> {
					private static class Inner {\
						public Inner(){}
					}
					<R extends Inner> void print(I<R> i) {}
					public Inner get() {
						return new Inner();
					}
				}
				""",
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Test<String> t = new Test<>();
						t.print(new I<>() {
							public String toString() {
								return "";
							}
						});
					}
				}
				interface I<T> {
					String toString();
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				t.print(new I<>() {
				            ^^^^^
			The type Test$Inner is not visible
			----------
			""");
}
// All non-private methods of an anonymous class instantiated with '<>' must be treated as being annotated with @override
public void testBug517926() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String name;
					public X(String name) {
						this.name = name;
					}
					<T> void print(T o, I<T> converter) {
						System.out.println(converter.toString(o));
					}
					String name() {
						return this.name;
					}
					public static void main(String[] args) {
						X x = new X("asdasfd");
						x.print(x, new I<>() {
							public String name() {return null;}
							public String toString(X xx) {
								return xx.toString();
							}
						});
					}
				}
				interface I<T> {
				private String name() {return null;}\
					String toString(T t);
				default String getName() {return name();}\
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				public String name() {return null;}
				              ^^^^^^
			The method name() of type new I<X>(){} must override or implement a supertype method
			----------
			""");
}
public void testBug521815a() {
	runNegativeTest(
			new String[] {
					"a/b/X.java",
					"""
						package a.b;
						interface I{
						    public static class Inner { }
						}
						class Cl {
						    public static class Inner {}
						}
						public class X extends Cl implements I {}
						""",
					"a/Y.java",
					"""
						package p;
						import static a.b.X.Inner;
						public class Y {;
							Inner t;
						}
						"""
			},
			"""
				----------
				1. ERROR in a\\Y.java (at line 4)
					Inner t;
					^^^^^
				The type Inner is ambiguous
				----------
				""");
}
public void testBug521815b() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_8) {
		return;
	}
	runNegativeTest(
			new String[] {
					"a/b/X.java",
					"""
						package a.b;
						interface I{
						    public static class Inner { }
						}
						class Cl {
						    public static class Inner {}
						}
						public class X extends Cl implements I {}
						""",
					"a/Y.java",
					"""
						package p;
						import static a.b.X.Inner;
						public class Y {;
						}
						"""
			},
			"""
				----------
				1. WARNING in a\\Y.java (at line 2)
					import static a.b.X.Inner;
					              ^^^^^^^^^^^
				The import a.b.X.Inner is never used
				----------
				""");
}
public void testBug533644() {
	runConformTest(
		new String[] {
			"q/JobDetail.java",
			"""
				package q;
				import java.io.Serializable;
				public interface JobDetail extends Serializable, Cloneable { }
				""",
			"q/Scheduler.java",
			"""
				package q;
				import java.util.Map;
				import java.util.Set;
				public interface Scheduler {
				    void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException;
				}
				""",
			"q/SchedulerException.java",
			"""
				package q;
				public class SchedulerException extends Exception {
				    private static final long serialVersionUID = 174841398690789156L;
				}
				""",
			"q/Trigger.java",
			"""
				package q;
				import java.io.Serializable;
				public interface Trigger extends Serializable, Cloneable, Comparable<Trigger> {
				    public static final long serialVersionUID = -3904243490805975570L;
				}
				"""
		});
	Runner runner = new Runner();
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"ForwardingScheduler.java",
			"""
				import java.util.Map;
				import java.util.Set;
				
				import q.JobDetail;
				import q.Scheduler;
				import q.SchedulerException;
				import q.Trigger;
				
				public class ForwardingScheduler implements Scheduler {
				  @Override
				  public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace)
				      throws SchedulerException {
				  }
				}
				"""
	};
	runner.runConformTest();
}
//As All non-private methods of an anonymous class instantiated with '<>' must be treated as being annotated with @override,
//"Remove redundant type arguments" diagnostic should be reported ONLY if all the non-private methods defined in the anonymous class
//are also present in the parent class.
public void testBug551913_001() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int foo() {
						java.util.HashSet<String> a = new java.util.HashSet<>();
						java.util.HashSet<String> b = new java.util.HashSet<String>(a) {
							private static final long serialVersionUID = 1L;
							public int x() {return 10;}
						};
						return 10;
					}
				
					public static void main(String[] args) {
						X abc= new X();
						System.out.println(abc.foo());\
					}\
				}""",
		},"10", options);
}
// As All non-private methods of an anonymous class instantiated with '<>' must be treated as being annotated with @override,
// "Remove redundant type arguments" diagnostic should be reported ONLY if all the non-private methods defined in the anonymous class
// are also present in the parent class.
public void testBug551913_002() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						java.util.HashSet<String> a = new java.util.HashSet<>();
						java.util.HashSet<String> b = new java.util.HashSet<String>(a) {
							private static final long serialVersionUID = 1L;
							public String toString() {return null;}
						};
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				java.util.HashSet<String> b = new java.util.HashSet<String>(a) {
				                                            ^^^^^^^
			Redundant specification of type arguments <String>
			----------
			""",
		null, true, options);
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=551913
public void testBug551913_003() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						java.util.HashSet<String> a = new java.util.HashSet<>();
						java.util.HashSet<String> b = new java.util.HashSet<String>(a) {
							private static final long serialVersionUID = 1L;
							public String toString() {return asString();}
				           private String asString() { return null;}
						};
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				java.util.HashSet<String> b = new java.util.HashSet<String>(a) {
				                                            ^^^^^^^
			Redundant specification of type arguments <String>
			----------
			""",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=551913
public void testBug551913_004() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						java.util.HashSet<String> a = new java.util.HashSet<>();
						java.util.HashSet<String> b = new java.util.HashSet<String>(a) {
							private static final long serialVersionUID = 1L;
							public String toString() {return asString();}
				           public String asString() { return null;}
						};
					}
				}""",
		},
		"",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// Recommendation from compiler to drop type arguments leads to compile error
public void testGH1506() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.util.Arrays;
				import java.util.Iterator;
				
				public class X {
				
					public Iterable<File> getStackFramesClassesLocations(Object element) {
						return new Iterable<File>() {
							@Override
							public Iterator<File> iterator() {
								return Arrays.stream(new Object[0]) //
										.map(frame -> getClassesLocation(frame)) //
										.iterator();
							}
						\t
							File getClassesLocation(Object frame) {
								return null;
							}
						};
					}
				}
				""",
		},
		"",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// Recommendation from compiler to drop type arguments leads to compile error
public void testGH1506_2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.File;
				import java.util.Arrays;
				import java.util.Iterator;
				
				public class X {
				
					public Iterable<File> getStackFramesClassesLocations(Object element) {
						return new Iterable<File>() {
							@Override
							public Iterator<File> iterator() {
								return Arrays.stream(new Object[0]) //
										.map(frame -> getClassesLocation(frame)) //
										.iterator();
							}
						\t
							private File getClassesLocation(Object frame) {
								return null;
							}
						};
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				return new Iterable<File>() {
				           ^^^^^^^^
			Redundant specification of type arguments <File>
			----------
			""",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// Recommendation from compiler to drop type arguments leads to compile error
public void testGH1506_3() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<E> {
				    static class AX<T>{}
				    X(E e) {}
				    X() {}
				    public static void main(String[] args) {
				    	X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { private void foo() {} });
					}
				}\s
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { private void foo() {} });
				            ^^
			X.AX is a raw type. References to generic type X.AX<T> should be parameterized
			----------
			2. WARNING in X.java (at line 6)
				X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { private void foo() {} });
				                                                                       ^^^^^
			The method foo() from the type new X.AX<String>(){} is never used locally
			----------
			""",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1506
// Recommendation from compiler to drop type arguments leads to compile error
public void testGH1506_4() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X<E> {
				    static class AX<T>{}
				    X(E e) {}
				    X() {}
				    public static void main(String[] args) {
				    	X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { public void foo() {} });
					}
				}\s
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { public void foo() {} });
				            ^^
			X.AX is a raw type. References to generic type X.AX<T> should be parameterized
			----------
			2. WARNING in X.java (at line 6)
				X<? extends AX> x5 = new X<AX<String>>(new AX<String>() { public void foo() {} });
				                                                                      ^^^^^
			The method foo() from the type new X.AX<String>(){} is never used locally
			----------
			""",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1560
// ECJ recommends diamond when using it would result in non-denotable types.
public void testGH1560() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
			import java.util.Collection;
			import java.util.List;

			public class X<S, D> {

				public interface MMenuElement {}
				public interface IObservable {}
				public static class ListDiffVisitor<E> {}
				public interface IObservablesListener {}
				public interface IObservableCollection<E> extends IObservable, Collection<E> {}
				public static class ObservableEvent {}
				public interface IDiff {}
				public static class ListDiff<E> implements IDiff {
					public void accept(ListDiffVisitor<? super E> visitor) {}
				}
				public static class ListChangeEvent<E> extends ObservableEvent {
					public ListDiff<E> diff;
				}
				public interface IListChangeListener<E> extends IObservablesListener {
					void handleListChange(ListChangeEvent<? extends E> event);
				}
				public interface IObservableList<E> extends List<E>, IObservableCollection<E> {
					void addListChangeListener(IListChangeListener<? super E> listener);
				}

				public void foo() {

					IObservableList<MMenuElement> l;

					l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<MMenuElement>() {})); // <> should not be recommended here!!!

				}
			}
			""",
		},
		"""
			----------
			1. ERROR in X.java (at line 30)
				l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<MMenuElement>() {})); // <> should not be recommended here!!!
				^
			The local variable l may not have been initialized
			----------
			""",
		null, true, options);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1560
// ECJ recommends diamond when using it would result in non-denotable types.
public void testGH1560_2() {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRedundantSpecificationOfTypeArguments, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
			import java.util.Collection;
			import java.util.List;

			public class X<S, D> {

				public interface MMenuElement {}
				public interface IObservable {}
				public static class ListDiffVisitor<E> {}
				public interface IObservablesListener {}
				public interface IObservableCollection<E> extends IObservable, Collection<E> {}
				public static class ObservableEvent {}
				public interface IDiff {}
				public static class ListDiff<E> implements IDiff {
					public void accept(ListDiffVisitor<? super E> visitor) {}
				}
				public static class ListChangeEvent<E> extends ObservableEvent {
					public ListDiff<E> diff;
				}
				public interface IListChangeListener<E> extends IObservablesListener {
					void handleListChange(ListChangeEvent<? extends E> event);
				}
				public interface IObservableList<E> extends List<E>, IObservableCollection<E> {
					void addListChangeListener(IListChangeListener<? super E> listener);
				}

				public void foo() {

					IObservableList<MMenuElement> l;

					l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<>() {})); // non-denotable type error

				}
			}
			""",
		},
		"""
			----------
			1. ERROR in X.java (at line 30)
				l.addListChangeListener(event -> event.diff.accept(new ListDiffVisitor<>() {})); // non-denotable type error
				                                                       ^^^^^^^^^^^^^^^
			Type X.ListDiffVisitor<capture#1-of ? extends X.MMenuElement> inferred for ListDiffVisitor<>, is not valid for an anonymous class with '<>'
			----------
			""",
		null, true, options);
}
public static Class<GenericsRegressionTest_9> testClass() {
	return GenericsRegressionTest_9.class;
}

}
