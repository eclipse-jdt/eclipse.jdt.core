/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class InnerClass15Test extends AbstractRegressionTest {
public InnerClass15Test(String name) {
	super(name);
}
static {
//	TESTS_NUMBERS = new int[] { 2 };
	//TESTS_NAMES = new String[] {"testBug520874"};
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
@Override
protected Map<String, String> getCompilerOptions() {
	Map<String, String> options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	return options;
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test001() {
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			class X {
				<X> void foo() {
					class X {}
				}
			}""",
	},
	"""
		----------
		1. WARNING in X.java (at line 2)
			<X> void foo() {
			 ^
		The type parameter X is hiding the type X
		----------
		2. WARNING in X.java (at line 3)
			class X {}
			      ^
		The nested type X is hiding the type parameter X of the generic method foo() of type X
		----------
		3. ERROR in X.java (at line 3)
			class X {}
			      ^
		The nested type X cannot hide an enclosing type
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test002() {
	this.runNegativeTest(new String[] {
		"X.java",
		"""
			class X<X> {
				void foo() {
					class X {}
				}
			}""",
	},
	"""
		----------
		1. WARNING in X.java (at line 1)
			class X<X> {
			        ^
		The type parameter X is hiding the type X<X>
		----------
		2. WARNING in X.java (at line 3)
			class X {}
			      ^
		The nested type X is hiding the type parameter X of type X<X>
		----------
		3. ERROR in X.java (at line 3)
			class X {}
			      ^
		The nested type X cannot hide an enclosing type
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
// note javac reports an error for this test, but that is
// incorrect, compare and contrast javac behavior with
// test004.
public void test003() {
	this.runNegativeTest(new String[] {
		"Y.java",
		"""
			class Y {
			class X {}
				<X> void foo() {
					class X {}
				}
			}""",
	},
	"""
		----------
		1. WARNING in Y.java (at line 3)
			<X> void foo() {
			 ^
		The type parameter X is hiding the type Y.X
		----------
		2. WARNING in Y.java (at line 4)
			class X {}
			      ^
		The nested type X is hiding the type parameter X of the generic method foo() of type Y
		----------
		3. WARNING in Y.java (at line 4)
			class X {}
			      ^
		The type X is never used locally
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312989
public void test004() {
	this.runNegativeTest(new String[] {
		"Y.java",
		"""
			class Y {
			class X {}
			   void foo() {
					class X {}
				}
			}""",
	},
	"""
		----------
		1. WARNING in Y.java (at line 4)
			class X {}
			      ^
		The type X is hiding the type Y.X
		----------
		2. WARNING in Y.java (at line 4)
			class X {}
			      ^
		The type X is never used locally
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test005() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"""
			package p1;
			import static p1.BrownBox.*;
			public interface GreenBox {
			    public static class Cat extends Object {}
			}
			""",
		"p1/BrownBox.java",
		"""
			package p1;
			import static p1.GreenBox.*;
			public interface BrownBox {
			    public static class BlackCat extends Cat {}
			}
			""",
	},
	"""
		----------
		1. WARNING in p1\\GreenBox.java (at line 2)
			import static p1.BrownBox.*;
			              ^^^^^^^^^^^
		The import p1.BrownBox is never used
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test006() {
	this.runNegativeTest(new String[] {
		"p1/BrownBox.java",
		"""
			package p1;
			import static p1.GreenBox.*;
			public interface BrownBox {
			    public static class BlackCat extends Cat {}
			}
			""",
		"p1/GreenBox.java",
		"""
			package p1;
			import static p1.BrownBox.*;
			public interface GreenBox {
			    public static class Cat extends Object {}
			}
			""",
	},
	"""
		----------
		1. WARNING in p1\\GreenBox.java (at line 2)
			import static p1.BrownBox.*;
			              ^^^^^^^^^^^
		The import p1.BrownBox is never used
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test007() {
	this.runNegativeTest(new String[] {
		"p1/BrownBox.java",
		"""
			package p1;
			import static p1.GreenBox.*;
			public interface BrownBox {
			    public static class BlackCat extends Cat {}
			}
			""",
		"p1/GreenBox.java",
		"""
			package p1;
			import static p1.BrownBox.*;
			public interface GreenBox {
			    public static class Cat extends java.lang.Object {}
			}
			""",
	},
	"""
		----------
		1. WARNING in p1\\GreenBox.java (at line 2)
			import static p1.BrownBox.*;
			              ^^^^^^^^^^^
		The import p1.BrownBox is never used
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test008() {
	this.runNegativeTest(new String[] {
		"p1/BrownBox.java",
		"""
			package p1;
			import static p1.GreenBox.*;
			public interface BrownBox {
			    public static class BlackCat extends Cat {}
			}
			""",
		"p1/GreenBox.java",
		"""
			package p1;
			import static p1.BrownBox.*;
			public interface GreenBox {
			    public static class Cat extends BlackCat {}
			}
			""",
	},
	"""
		----------
		1. ERROR in p1\\BrownBox.java (at line 4)
			public static class BlackCat extends Cat {}
			                    ^^^^^^^^
		The hierarchy of the type BlackCat is inconsistent
		----------
		----------
		1. ERROR in p1\\GreenBox.java (at line 4)
			public static class Cat extends BlackCat {}
			                                ^^^^^^^^
		Cycle detected: a cycle exists in the type hierarchy between GreenBox.Cat and BrownBox.BlackCat
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test009() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"""
			package p1;
			import static p1.BrownBox.*;
			public interface GreenBox {
			    public static class Cat extends BlackCat {}
			}
			""",
		"p1/BrownBox.java",
		"""
			package p1;
			import static p1.GreenBox.*;
			public interface BrownBox {
			    public static class BlackCat extends Cat {}
			}
			""",
	},
	"""
		----------
		1. ERROR in p1\\GreenBox.java (at line 4)
			public static class Cat extends BlackCat {}
			                    ^^^
		The hierarchy of the type Cat is inconsistent
		----------
		----------
		1. ERROR in p1\\BrownBox.java (at line 4)
			public static class BlackCat extends Cat {}
			                                     ^^^
		Cycle detected: a cycle exists in the type hierarchy between BrownBox.BlackCat and GreenBox.Cat
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0010() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"""
			package p1;
			import static p1.BrownBox.*;
			interface SuperInterface {
			   public static class Cat extends BlackCat {}
			}
			public interface GreenBox {
			}
			""",
		"p1/BrownBox.java",
		"""
			package p1;
			import static p1.GreenBox.*;
			public interface BrownBox {
			    public static class BlackCat extends Cat {}
			}
			""",
	},
	"""
		----------
		1. ERROR in p1\\GreenBox.java (at line 4)
			public static class Cat extends BlackCat {}
			                    ^^^
		The hierarchy of the type Cat is inconsistent
		----------
		----------
		1. ERROR in p1\\BrownBox.java (at line 4)
			public static class BlackCat extends Cat {}
			                                     ^^^
		Cat cannot be resolved to a type
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0011() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"""
			package p1;
			import static p1.BrownBox.*;
			interface SuperInterface {
			   public static class Cat extends BlackCat {}
			}
			public interface GreenBox extends SuperInterface {
			}
			""",
		"p1/BrownBox.java",
		"""
			package p1;
			import static p1.GreenBox.*;
			public interface BrownBox {
			    public static class BlackCat extends Cat {}
			}
			""",
	},
	"""
		----------
		1. ERROR in p1\\GreenBox.java (at line 4)
			public static class Cat extends BlackCat {}
			                    ^^^
		The hierarchy of the type Cat is inconsistent
		----------
		----------
		1. ERROR in p1\\BrownBox.java (at line 4)
			public static class BlackCat extends Cat {}
			                                     ^^^
		Cycle detected: a cycle exists in the type hierarchy between BrownBox.BlackCat and SuperInterface.Cat
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0012() {
	this.runNegativeTest(new String[] {
		"p1/GreenBox.java",
		"""
			package p1;
			import static p1.BrownBox.*;
			interface SuperInterface {
			   public static class Cat extends BlackCat {}
			}
			public interface GreenBox extends SuperInterface {
			}
			""",
		"p1/BrownBox.java",
		"""
			package p1;
			import static p1.GreenBox.*;
			public interface BrownBox {
			    public static class BlackCat extends GreenBox.Cat {}
			}
			""",
	},
	"""
		----------
		1. ERROR in p1\\GreenBox.java (at line 4)
			public static class Cat extends BlackCat {}
			                    ^^^
		The hierarchy of the type Cat is inconsistent
		----------
		----------
		1. ERROR in p1\\BrownBox.java (at line 4)
			public static class BlackCat extends GreenBox.Cat {}
			                                     ^^^^^^^^^^^^
		Cycle detected: a cycle exists in the type hierarchy between BrownBox.BlackCat and SuperInterface.Cat
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0013() {
	this.runNegativeTest(new String[] {
		"cycle/X.java",
		"""
			package cycle;
			class X extends Y {}
			class Y extends X {}
			""",
	},
	"""
		----------
		1. ERROR in cycle\\X.java (at line 2)
			class X extends Y {}
			      ^
		The hierarchy of the type X is inconsistent
		----------
		2. ERROR in cycle\\X.java (at line 3)
			class Y extends X {}
			                ^
		Cycle detected: a cycle exists in the type hierarchy between Y and X
		----------
		""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319885
public void test0014() {
	this.runNegativeTest(new String[] {
		"cycle/X.java",
		"""
			package cycle;
			class X extends Y {}
			class Y extends Z {}
			class Z extends A {}
			class A extends B {}
			class B extends C {}
			class C extends X {}
			"""
	},
	"""
		----------
		1. ERROR in cycle\\X.java (at line 2)
			class X extends Y {}
			      ^
		The hierarchy of the type X is inconsistent
		----------
		2. ERROR in cycle\\X.java (at line 3)
			class Y extends Z {}
			      ^
		The hierarchy of the type Y is inconsistent
		----------
		3. ERROR in cycle\\X.java (at line 4)
			class Z extends A {}
			      ^
		The hierarchy of the type Z is inconsistent
		----------
		4. ERROR in cycle\\X.java (at line 5)
			class A extends B {}
			      ^
		The hierarchy of the type A is inconsistent
		----------
		5. ERROR in cycle\\X.java (at line 6)
			class B extends C {}
			      ^
		The hierarchy of the type B is inconsistent
		----------
		6. ERROR in cycle\\X.java (at line 7)
			class C extends X {}
			                ^
		Cycle detected: a cycle exists in the type hierarchy between C and X
		----------
		""");
}
public void testBug520874a() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/A.java",
			"""
				package p;
				class A extends C {
				    static class B {}
				}
				""",
			"cycle/X.java",
			"""
				package p;
				import p.A.B;
				class C extends B {}
				public class X {
				    public static void main(String argv[]) {
				      new C();
				    }
				}
				""",
		},
			"""
				----------
				1. ERROR in cycle\\A.java (at line 2)
					class A extends C {
					      ^
				The hierarchy of the type A is inconsistent
				----------
				----------
				1. ERROR in cycle\\X.java (at line 3)
					class C extends B {}
					                ^
				Cycle detected: a cycle exists in the type hierarchy between C and A
				----------
				""");
}
public void testBug520874b() {
	if (this.complianceLevel < ClassFileConstants.JDK9)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"""
				package p;
				import p.A.*;
				class C extends B {}
				public class X {
				    public static void main(String argv[]) {
				      new C();
				    }
				}
				""",
			"cycle/A.java",
			"""
				package p;
				class A extends C {
				    static class B {}
				}
				"""
		},
			"""
				----------
				1. ERROR in cycle\\X.java (at line 3)
					class C extends B {}
					      ^
				The hierarchy of the type C is inconsistent
				----------
				----------
				1. ERROR in cycle\\A.java (at line 2)
					class A extends C {
					                ^
				Cycle detected: a cycle exists in the type hierarchy between A and C
				----------
				""");
}
public void testBug520874c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"""
				package cycle;
				import cycle.A.B;
				class C implements B {}
				public class X {
				    public static void main(String argv[]) {
				      new C();
				    }
				}
				""",
			"cycle/A.java",
			"""
				package cycle;
				class A extends C {
				    static interface B {}
				}
				"""
		},
			"""
				----------
				1. ERROR in cycle\\X.java (at line 3)
					class C implements B {}
					      ^
				The hierarchy of the type C is inconsistent
				----------
				----------
				1. ERROR in cycle\\A.java (at line 2)
					class A extends C {
					                ^
				Cycle detected: a cycle exists in the type hierarchy between A and C
				----------
				""");
}
public void testBug520874d() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"""
				package cycle;
				import cycle.A.*;
				class C implements B {}
				public class X {
				    public static void main(String argv[]) {
				      new C();
				    }
				}
				""",
			"cycle/A.java",
			"""
				package cycle;
				class A extends C {
				    static interface B {}
				}
				"""
		},
			"""
				----------
				1. ERROR in cycle\\X.java (at line 3)
					class C implements B {}
					      ^
				The hierarchy of the type C is inconsistent
				----------
				----------
				1. ERROR in cycle\\A.java (at line 2)
					class A extends C {
					                ^
				Cycle detected: a cycle exists in the type hierarchy between A and C
				----------
				""");
}
public void testBug520874e() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"""
				package cycle;
				import cycle.A.B;
				interface C extends B {}
				public class X {
				    public static void main(String argv[]) {
				    }
				}
				""",
			"cycle/A.java",
			"""
				package cycle;
				class A extends C {
				    static interface B {}
				}
				"""
		},
			"""
				----------
				1. ERROR in cycle\\X.java (at line 3)
					interface C extends B {}
					          ^
				The hierarchy of the type C is inconsistent
				----------
				----------
				1. ERROR in cycle\\A.java (at line 2)
					class A extends C {
					                ^
				Cycle detected: a cycle exists in the type hierarchy between A and C
				----------
				""");
}
public void testBug520874f() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"""
				package cycle;
				import cycle.A.*;
				interface C extends B {}
				public class X {
				    public static void main(String argv[]) {
				    }
				}
				""",
			"cycle/A.java",
			"""
				package cycle;
				class A extends C {
				    static interface B {}
				}
				"""
		},
			"""
				----------
				1. ERROR in cycle\\X.java (at line 3)
					interface C extends B {}
					          ^
				The hierarchy of the type C is inconsistent
				----------
				----------
				1. ERROR in cycle\\A.java (at line 2)
					class A extends C {
					                ^
				Cycle detected: a cycle exists in the type hierarchy between A and C
				----------
				""");
}
public void testBug520874g() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"""
				package cycle;
				import cycle.A.B;
				interface C extends B {}
				public class X {
				    public static void main(String argv[]) {
				    }
				}
				""",
			"cycle/A.java",
			"""
				package cycle;
				interface A extends C {
				    static interface B {}
				}
				"""
		},
			"""
				----------
				1. ERROR in cycle\\X.java (at line 3)
					interface C extends B {}
					          ^
				The hierarchy of the type C is inconsistent
				----------
				----------
				1. ERROR in cycle\\A.java (at line 2)
					interface A extends C {
					                    ^
				Cycle detected: a cycle exists in the type hierarchy between A and C
				----------
				""");
}
public void testBug520874h() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"""
				package cycle;
				import cycle.A.*;
				interface C extends B {}
				public class X {
				    public static void main(String argv[]) {
				    }
				}
				""",
			"cycle/A.java",
			"""
				package cycle;
				interface A extends C {
				    static interface B {}
				}
				"""
		},
			"""
				----------
				1. ERROR in cycle\\X.java (at line 3)
					interface C extends B {}
					          ^
				The hierarchy of the type C is inconsistent
				----------
				----------
				1. ERROR in cycle\\A.java (at line 2)
					interface A extends C {
					                    ^
				Cycle detected: a cycle exists in the type hierarchy between A and C
				----------
				""");
}
public void testBug520874i() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // Limit the new tests to newer levels
	this.runNegativeTest(new String[] {
			"cycle/X.java",
			"""
				package cycle;
				import cycle.A.*;
				interface C extends A {}
				public class X {
				    public static void main(String argv[]) {
				    }
				}
				""",
			"cycle/A.java",
			"""
				package cycle;
				interface A extends C {
				    static interface B {}
				}
				"""
		},
		"""
			----------
			1. ERROR in cycle\\X.java (at line 3)
				interface C extends A {}
				          ^
			The hierarchy of the type C is inconsistent
			----------
			----------
			1. ERROR in cycle\\A.java (at line 2)
				interface A extends C {
				                    ^
			Cycle detected: a cycle exists in the type hierarchy between A and C
			----------
			""");
}
public void testBug526681() {
	runNegativeTest(
		new String[] {
			"p/A.java",
			"""
				package p;
				import p.B;
				public class A extends B {
					public static abstract class C {}
				}
				""",
			"p/B.java",
			"""
				package p;
				import p.A.C;
				public abstract class B extends C {}"""
		},
		"""
			----------
			1. ERROR in p\\A.java (at line 3)
				public class A extends B {
				             ^
			The hierarchy of the type A is inconsistent
			----------
			----------
			1. ERROR in p\\B.java (at line 3)
				public abstract class B extends C {}
				                                ^
			Cycle detected: a cycle exists in the type hierarchy between B and A
			----------
			""");
}
public void testBug527731() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses diamond, 1.7-inference fails, only 1.8 is good
	runConformTest(
		new String[] {
			"OuterClass.java",
			"""
				import java.util.ArrayList;
				
				public class OuterClass<T> extends ArrayList<OuterClass.InnerTypedClass<T>> {
				\t
					public static interface InnerInterface {}
				\t
					public static class InnerTypedClass<T> implements InnerInterface {}
				\t
					public static void main(String[] args) {
						OuterClass<String> outerClass = new OuterClass<>();
						outerClass.add(new InnerTypedClass<>());
						System.out.println(outerClass);
					}
				}
				"""
		});
}
public static Class<InnerClass15Test> testClass() {
	return InnerClass15Test.class;
}
}
