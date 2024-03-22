/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contributions for
 *								Bug 365519 - editorial cleanup after bug 186342 and bug 365387
 *								Bug 265744 - Enum switch should warn about missing default
 *								Bug 374605 - Unreasonable warning for enum-based switch statements
 *								bug 388739 - [1.8][compiler] consider default methods when detecting whether a class needs to be declared abstract
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class EnumTest extends AbstractComparableTest {

	String reportMissingJavadocComments = null;

	public EnumTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test187" };
//		TESTS_NUMBERS = new int[] { 185 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildComparableTestSuite(testClass());
	}

	public static Class testClass() {
		return EnumTest.class;
	}

	@Override
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotationForInterfaceMethodImplementation, CompilerOptions.DISABLED);
		options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
		if (this.reportMissingJavadocComments != null)
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.reportMissingJavadocComments = null;
	}

// test simple valid enum and its usage
public void test000() {
	runConformTest(
		new String[] {
			"e/X.java",
			"""
				package e;
				import e.T;
				import static e.T.*;
				
				public class X {
				    public static void main(String[] args) {
				    	System.out.print("JDTCore team:");
				    	T oldest = null;
				    	int maxAge = Integer.MIN_VALUE;
				    	for (T t : T.values()) {
				            if (t == YODA) continue;// skip YODA
				            t.setRole(t.isManager());
							 if (t.age() > maxAge) {
				               oldest = t;
				               maxAge = t.age();
				            }
				            System.out.print(" "+ t + ':'+t.age()+':'+location(t)+':'+t.role);
				        }
				        System.out.println(" WINNER is:" + T.valueOf(oldest.name()));
				    }
				
				   private enum Location { SNZ, OTT }
				
				    private static Location location(T t) {
				        switch(t) {
				          case PHILIPPE: \s
				          case DAVID:
				          case JEROME:
				          case FREDERIC:
				          	return Location.SNZ;
				          case OLIVIER:
				          case KENT:
				            return Location.OTT;
				          default:
				            throw new AssertionError("Unknown team member: " + t);
				        }
				    }
				}
				""",
			"e/T.java",
			"""
				package e;
				public enum T {
					PHILIPPE(37) {
						public boolean isManager() {
							return true;
						}
					},
					DAVID(27),
					JEROME(33),
					OLIVIER(35),
					KENT(40),
					YODA(41),
					FREDERIC;
					final static int OLD = 41;
				
				   enum Role { M, D }
				
				   int age;
					Role role;
				
					T() { this(OLD); }
					T(int age) {
						this.age = age;
					}
					public int age() { return this.age; }
					public boolean isManager() { return false; }
					void setRole(boolean mgr) {
						this.role = mgr ? Role.M : Role.D;
					}
				}
				"""
		},
		"JDTCore team: PHILIPPE:37:SNZ:M DAVID:27:SNZ:D JEROME:33:SNZ:D OLIVIER:35:OTT:D KENT:40:OTT:D FREDERIC:41:SNZ:D WINNER is:FREDERIC"
	);
}
// check assignment to enum constant is disallowed
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
					BLEU,\s
					BLANC,\s
					ROUGE;
					static {
						BLEU = null;
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				BLEU = null;
				^^^^
			The final field X.BLEU cannot be assigned
			----------
			""");
}
// check diagnosis for duplicate enum constants
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU,\s
					BLANC,\s
					ROUGE,
					BLEU;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				BLEU,\s
				^^^^
			Duplicate field X.BLEU
			----------
			2. ERROR in X.java (at line 6)
				BLEU;
				^^^^
			Duplicate field X.BLEU
			----------
			""");
}
// check properly rejecting enum constant modifiers
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					public BLEU,\s
					transient BLANC,\s
					ROUGE,\s
					abstract RED {
						void test() {}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				public BLEU,\s
				       ^^^^
			Illegal modifier for the enum constant BLEU; no modifier is allowed
			----------
			2. ERROR in X.java (at line 4)
				transient BLANC,\s
				          ^^^^^
			Illegal modifier for the enum constant BLANC; no modifier is allowed
			----------
			3. ERROR in X.java (at line 6)
				abstract RED {
				         ^^^
			Illegal modifier for the enum constant RED; no modifier is allowed
			----------
			""");
}
// check using an enum constant
public void test004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU,
					BLANC,
					ROUGE;
				\t
					public static void main(String[] args) {
						System.out.println(BLEU);
					}
				\t
				}
				"""
		},
		"BLEU");
}
// check method override diagnosis (with no enum constants)
public void test005() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
					;
					protected Object clone() { return this; }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				protected Object clone() { return this; }
				                 ^^^^^^^
			Cannot override the final method from Enum<X>
			----------
			2. WARNING in X.java (at line 3)
				protected Object clone() { return this; }
				                 ^^^^^^^
			The method clone() of type X should be tagged with @Override since it actually overrides a superclass method
			----------
			""");
}
// check generated #values() method
public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU,
					BLANC,
					ROUGE;
				\t
					public static void main(String[] args) {
						for(X x: X.values()) {
							System.out.print(x);
						}
					}
				\t
				}
				"""
		},
		"BLEUBLANCROUGE");
}
// tolerate user definition for $VALUES
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU,
					BLANC,
					ROUGE;
				\t
				   int $VALUES;
					public static void main(String[] args) {
						for(X x: X.values()) {
							System.out.print(x);
						}
					}
				\t
				}
				"""
		},
		"BLEUBLANCROUGE");
}
// reject user definition for #values()
public void test008() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU,
					BLANC,
					ROUGE;
				\t
				   void dup() {}\s
				   void values() {}\s
				   void dup() {}\s
				   void values() {}\s
				   Missing dup() {}\s
					public static void main(String[] args) {
						for(X x: X.values()) {
							System.out.print(x);
						}
					}
				\t
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				void dup() {}\s
				     ^^^^^
			Duplicate method dup() in type X
			----------
			2. ERROR in X.java (at line 8)
				void values() {}\s
				     ^^^^^^^^
			The enum X already defines the method values() implicitly
			----------
			3. ERROR in X.java (at line 9)
				void dup() {}\s
				     ^^^^^
			Duplicate method dup() in type X
			----------
			4. ERROR in X.java (at line 10)
				void values() {}\s
				     ^^^^^^^^
			The enum X already defines the method values() implicitly
			----------
			5. ERROR in X.java (at line 11)
				Missing dup() {}\s
				^^^^^^^
			Missing cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 11)
				Missing dup() {}\s
				        ^^^^^
			Duplicate method dup() in type X
			----------
			""");
}
// switch on enum
public void test009() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				\t
					BLEU,
					BLANC,
					ROUGE;
				\t
					//void values() {}
				\t
					public static void main(String[] args) {
						X x = BLEU;
						switch(x) {
							case BLEU :
								System.out.println("SUCCESS");
								break;
							case BLANC :
							case ROUGE :
								System.out.println("FAILED");
								break;
				           default: // nop
						}
					}
				\t
				}"""
		},
		"SUCCESS");
}
// duplicate switch case
public void test010() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				\t
					BLEU,
					BLANC,
					ROUGE;
				\t
					//void values() {}
				\t
					public static void main(String[] args) {
						X x = BLEU;
						switch(x) {
							case BLEU :
								break;
							case BLEU :
							case BLANC :
							case ROUGE :
								System.out.println("FAILED");
								break;
				           default: // nop
						}
					}
				\t
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				case BLEU :
				^^^^^^^^^
			Duplicate case
			----------
			2. ERROR in X.java (at line 14)
				case BLEU :
				^^^^^^^^^
			Duplicate case
			----------
			""");
}
// reject user definition for #values()
public void test011() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU,
					BLANC,
					ROUGE;
				\t
				   void values() {}\s
				   void values() {}\s
					public static void main(String[] args) {
						for(X x: X.values()) {
							System.out.print(x);
						}
					}
				\t
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				void values() {}\s
				     ^^^^^^^^
			The enum X already defines the method values() implicitly
			----------
			2. ERROR in X.java (at line 8)
				void values() {}\s
				     ^^^^^^^^
			The enum X already defines the method values() implicitly
			----------
			""");
}
// check abstract method diagnosis
public void test012() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X implements Runnable {\s
				\t
					BLEU,
					BLANC,
					ROUGE;
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public enum X implements Runnable {\s
				            ^
			The type X must implement the inherited abstract method Runnable.run()
			----------
			""");
}
// check enum constants with wrong arguments
public void test013() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU(10),
					BLANC(20),
					ROUGE(30);
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				BLEU(10),
				^^^^
			The constructor X(int) is undefined
			----------
			2. ERROR in X.java (at line 4)
				BLANC(20),
				^^^^^
			The constructor X(int) is undefined
			----------
			3. ERROR in X.java (at line 5)
				ROUGE(30);
				^^^^^
			The constructor X(int) is undefined
			----------
			""");
}
// check enum constants with extra arguments
public void test014() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU(10),
					BLANC(20),
					ROUGE(30);
				
					int val;
					X(int val) {
						this.val = val;
					}
				
					public static void main(String[] args) {
						for(X x: values()) {
							System.out.print(x.val);
						}
					}
				}
				"""
		},
		"102030");
}
// check enum constants with wrong arguments
public void test015() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
				\t
					BLEU(10),
					BLANC(),
					ROUGE(30);
				
					int val;
					X(int val) {
						this.val = val;
					}
				
					public static void main(String[] args) {
						for(X x: values()) {
							System.out.print(x.val);
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				BLANC(),
				^^^^^
			The constructor X() is undefined
			----------
			""");
}
// check enum constants with wrong arguments
public void test016() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				\t
					BLEU(10) {
						String foo() { // inner
							return super.foo() + this.val;
						}
					},
					BLANC(20),
					ROUGE(30);
				
					int val;
					X(int val) {
						this.val = val;
					}
					String foo() {  // outer
						return this.name();
					}
					public static void main(String[] args) {
						for(X x: values()) {
							System.out.print(x.foo());
						}
					}
				}
				"""
		},
		"BLEU10BLANCROUGE");
}
// check enum constants with empty arguments
public void test017() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				\t
					BLEU()
				}
				"""
		},
		"");
}
// cannot extend enums
public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					BLEU()
				}
				
				class XX extends X implements X {
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				class XX extends X implements X {
				                 ^
			The type X cannot be the superclass of XX; a superclass must be a class
			----------
			2. ERROR in X.java (at line 5)
				class XX extends X implements X {
				                              ^
			The type X cannot be a superinterface of XX; a superinterface must be an interface
			----------
			""");
}
// 74851
public void test019() {
	this.runConformTest(
		new String[] {
			"MonthEnum.java",
			"""
				public enum MonthEnum {
				    JANUARY   (30),
				    FEBRUARY  (28),
				    MARCH     (31),
				    APRIL     (30),
				    MAY       (31),
				    JUNE      (30),
				    JULY      (31),
				    AUGUST    (31),
				    SEPTEMBER (31),
				    OCTOBER   (31),
				    NOVEMBER  (30),
				    DECEMBER  (31);
				   \s
				    private final int days;
				   \s
				    MonthEnum(int days) {
				        this.days = days;
				    }
				   \s
				    public int getDays() {
				    	boolean leapYear = true;
				    	switch(this) {
				    		case FEBRUARY: if(leapYear) return days+1;
				           default: return days;
				    	}
				    }
				   \s
				    public static void main(String[] args) {
				    	System.out.println(JANUARY.getDays());
				    }
				   \s
				}
				""",
		},
		"30");
}
// 74226
public void test020() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo{
				    public enum Rank {FIRST,SECOND,THIRD}
				    public void setRank(Rank rank){}
				}
				""",
		},
		"");
}
// 74226 variation - check nested enum is implicitly static
public void test021() {
	this.runNegativeTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo {
				    public static enum Rank {FIRST,SECOND,THIRD;
				            void bar() { foo(); }\s
				    }
				    public void setRank(Rank rank){}
				    void foo() {}
				}
				""",
		},
		"""
			----------
			1. ERROR in Foo.java (at line 3)
				void bar() { foo(); }\s
				             ^^^
			Cannot make a static reference to the non-static method foo() from the type Foo
			----------
			""");
}
// 77151 - cannot use qualified name to denote enum constants in switch case label
public void test022() {
	if (this.complianceLevel >= ClassFileConstants.JDK21)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					enum MX { BLEU, BLANC, ROUGE }
				\t
					void foo(MX e) {
						switch(e) {
							case MX.BLEU : break;
							case MX.BLANC : break;
							case MX.ROUGE : break;
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				case MX.BLEU : break;
				     ^^^^^^^
			The qualified case label X.MX.BLEU must be replaced with the unqualified enum constant BLEU
			----------
			2. ERROR in X.java (at line 8)
				case MX.BLANC : break;
				     ^^^^^^^^
			The qualified case label X.MX.BLANC must be replaced with the unqualified enum constant BLANC
			----------
			3. ERROR in X.java (at line 9)
				case MX.ROUGE : break;
				     ^^^^^^^^
			The qualified case label X.MX.ROUGE must be replaced with the unqualified enum constant ROUGE
			----------
			""");
}

// 77212
public void test023() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public enum RuleType{ SUCCESS, FAILURE }
					public static void main(String[] args) {
						System.out.print(RuleType.valueOf(RuleType.SUCCESS.name()));
					}
				}""",
		},
		"SUCCESS");
}

// 77244 - cannot declare final enum
public void test024() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public final enum X {
					FOO() {}
				}
				
				""",
		},
	"""
		----------
		1. ERROR in X.java (at line 1)
			public final enum X {
			                  ^
		Illegal modifier for the enum X; only public is permitted
		----------
		""");
}

// values is using arraycopy instead of clone
public void test025() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					SUC, CESS;
					public static void main(String[] args) {
						for (X x : values()) {
							System.out.print(x.name());
						}
					}
				}""",
		},
		"SUCCESS");
}

// check enum name visibility
public void test026() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Couleur { BLEU, BLANC, ROUGE }
				}
				
				class Y {
					void foo(Couleur c) {
						switch (c) {
							case BLEU :
								break;
							case BLANC :
								break;
							case ROUGE :
								break;
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				void foo(Couleur c) {
				         ^^^^^^^
			Couleur cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 8)
				case BLEU :
				     ^^^^
			BLEU cannot be resolved to a variable
			----------
			3. ERROR in X.java (at line 10)
				case BLANC :
				     ^^^^^
			BLANC cannot be resolved to a variable
			----------
			4. ERROR in X.java (at line 12)
				case ROUGE :
				     ^^^^^
			ROUGE cannot be resolved to a variable
			----------
			""");
}
// check enum name visibility
public void test027() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Couleur { BLEU, BLANC, ROUGE }
					class Y {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
						}\t
					}
				}
				""",
		},
		"");
}
// check enum name visibility
public void test028() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Couleur {\s
						BLEU, BLANC, ROUGE;
						static int C = 0;
						static void FOO() {}
					}
					class Y {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
							FOO();
							C++;
						}\t
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 18)
				FOO();
				^^^
			The method FOO() is undefined for the type X.Y
			----------
			2. ERROR in X.java (at line 19)
				C++;
				^
			C cannot be resolved to a variable
			----------
			""");
}
// check enum name visibility
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Couleur {\s
						BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type
					}
					class Y {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
						}\t
					}
				}
				
				class BLEU {}
				""",
		},
		"");
}
// check enum name visibility
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Couleur {\s
						BLEU, BLANC, ROUGE; // take precedence over sibling constant from Color
					}
					enum Color {\s
						BLEU, BLANC, ROUGE;
					}
					class Y {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
						}\t
					}
				}
				
				class BLEU {}
				""",
		},
		"");
}
// check enum name visibility
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Couleur {\s
						BLEU, BLANC, ROUGE; // take precedence over toplevel BLEU type
					}
					class Y implements IX, JX {
						void foo(Couleur c) {
							switch (c) {
								case BLEU :
									break;
								case BLANC :
									break;
								case ROUGE :
									break;
				               default: // nop
							}
						}\t
					}
				}
				
				interface IX {
					int BLEU = 1;
				}
				interface JX {
					int BLEU = 2;
				}
				class BLEU {}
				
				""",
		},
		"");
}

// check Enum cannot be used as supertype (explicitly)
public void test032() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X extends Enum {\n" +
			"}",
		},
		"""
			----------
			1. WARNING in X.java (at line 1)
				public class X extends Enum {
				                       ^^^^
			Enum is a raw type. References to generic type Enum<E> should be parameterized
			----------
			2. ERROR in X.java (at line 1)
				public class X extends Enum {
				                       ^^^^
			The type X may not subclass Enum explicitly
			----------
			""");
}

// Javadoc in enum (see bug 78018)
public void test033() {
	this.runConformTest(
		new String[] {
			"E.java",
			"""
					/**
					 * Valid javadoc
					 * @author ffr
					 */
				public enum E {
					/** Valid javadoc */
					TEST,
					/** Valid javadoc */
					VALID;
					/** Valid javadoc */
					public void foo() {}
				}
				"""
		}
	);
}
public void test034() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
					/**
					 * Invalid javadoc
					 * @exception NullPointerException Invalid tag
					 * @throws NullPointerException Invalid tag
					 * @return Invalid tag
					 * @param x Invalid tag
					 */
				public enum E { TEST, VALID }
				"""
		},
		"""
			----------
			1. ERROR in E.java (at line 3)
				* @exception NullPointerException Invalid tag
				   ^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in E.java (at line 4)
				* @throws NullPointerException Invalid tag
				   ^^^^^^
			Javadoc: Unexpected tag
			----------
			3. ERROR in E.java (at line 5)
				* @return Invalid tag
				   ^^^^^^
			Javadoc: Unexpected tag
			----------
			4. ERROR in E.java (at line 6)
				* @param x Invalid tag
				   ^^^^^
			Javadoc: Unexpected tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test035() {
	this.runConformTest(
		new String[] {
			"E.java",
			"""
					/**
					 * @see "Valid normal string"
					 * @see <a href="http://download.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html">Valid URL link reference</a>
					 * @see Object
					 * @see #TEST
					 * @see E
					 * @see E#TEST
					 */
				public enum E { TEST, VALID }
				"""
		}
	);
}
public void test036() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
					/**
					 * @see "invalid" no text allowed after the string
					 * @see <a href="invalid">invalid</a> no text allowed after the href
					 * @see
					 * @see #VALIDE
					 */
				public enum E { TEST, VALID }
				"""
		},
		"""
			----------
			1. ERROR in E.java (at line 2)
				* @see "invalid" no text allowed after the string
				                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Unexpected text
			----------
			2. ERROR in E.java (at line 3)
				* @see <a href="invalid">invalid</a> no text allowed after the href
				                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Unexpected text
			----------
			3. ERROR in E.java (at line 4)
				* @see
				   ^^^
			Javadoc: Missing reference
			----------
			4. ERROR in E.java (at line 5)
				* @see #VALIDE
				        ^^^^^^
			Javadoc: VALIDE cannot be resolved or is not a field
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test037() {
	this.runConformTest(
		new String[] {
			"E.java",
			"""
					/**
					 * Value test: {@value #TEST}
					 * or: {@value E#TEST}
					 */
				public enum E { TEST, VALID }
				"""
		}
	);
}
public void test038() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
				public enum E { TEST, VALID;
					public void foo() {}
				}"""
		},
		"""
			----------
			1. ERROR in E.java (at line 1)
				public enum E { TEST, VALID;
				            ^
			Javadoc: Missing comment for public declaration
			----------
			2. ERROR in E.java (at line 1)
				public enum E { TEST, VALID;
				                ^^^^
			Javadoc: Missing comment for public declaration
			----------
			3. ERROR in E.java (at line 1)
				public enum E { TEST, VALID;
				                      ^^^^^
			Javadoc: Missing comment for public declaration
			----------
			4. ERROR in E.java (at line 2)
				public void foo() {}
				            ^^^^^
			Javadoc: Missing comment for public declaration
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test039() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
				public enum E {
					/**
					 * @exception NullPointerException Invalid tag
					 * @throws NullPointerException Invalid tag
					 * @return Invalid tag
					 * @param x Invalid tag
					 */
					TEST,
					VALID;
				}
				"""
		},
		"""
			----------
			1. ERROR in E.java (at line 3)
				* @exception NullPointerException Invalid tag
				   ^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in E.java (at line 4)
				* @throws NullPointerException Invalid tag
				   ^^^^^^
			Javadoc: Unexpected tag
			----------
			3. ERROR in E.java (at line 5)
				* @return Invalid tag
				   ^^^^^^
			Javadoc: Unexpected tag
			----------
			4. ERROR in E.java (at line 6)
				* @param x Invalid tag
				   ^^^^^
			Javadoc: Unexpected tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test040() {
	this.runConformTest(
		new String[] {
			"E.java",
			"""
				public enum E {
					/**
					 * @see E
					 * @see #VALID
					 */
					TEST,
					/**
					 * @see E#TEST
					 * @see E
					 */
					VALID;
					/**
					 * @param x the object
					 * @return String
					 * @see Object
					 */
					public String val(Object x) { return x.toString(); }
				}
				"""
		}
	);
}
public void test041() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
				public enum E {
					/**
					 * @see e
					 * @see #VALIDE
					 */
					TEST,
					/**
					 * @see E#test
					 * @see EUX
					 */
					VALID;
					/**
					 * @param obj the object
					 * @return
					 * @see Objet
					 */
					public String val(Object x) { return x.toString(); }
				}
				"""
		},
		"""
			----------
			1. ERROR in E.java (at line 3)
				* @see e
				       ^
			Javadoc: e cannot be resolved to a type
			----------
			2. ERROR in E.java (at line 4)
				* @see #VALIDE
				        ^^^^^^
			Javadoc: VALIDE cannot be resolved or is not a field
			----------
			3. ERROR in E.java (at line 8)
				* @see E#test
				         ^^^^
			Javadoc: test cannot be resolved or is not a field
			----------
			4. ERROR in E.java (at line 9)
				* @see EUX
				       ^^^
			Javadoc: EUX cannot be resolved to a type
			----------
			5. ERROR in E.java (at line 13)
				* @param obj the object
				         ^^^
			Javadoc: Parameter obj is not declared
			----------
			6. ERROR in E.java (at line 14)
				* @return
				   ^^^^^^
			Javadoc: Description expected after @return
			----------
			7. ERROR in E.java (at line 15)
				* @see Objet
				       ^^^^^
			Javadoc: Objet cannot be resolved to a type
			----------
			8. ERROR in E.java (at line 17)
				public String val(Object x) { return x.toString(); }
				                         ^
			Javadoc: Missing tag for parameter x
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void test042() {
	this.runConformTest(
		new String[] {
			"E.java",
			"""
				public enum E {
					/**
					 * Test value: {@value #TEST}
					 */
					TEST,
					/**
					 * Valid value: {@value E#VALID}
					 */
					VALID;
					/**
					 * Test value: {@value #TEST}
					 * Valid value: {@value E#VALID}
					 * @param x the object
					 * @return String
					 */
					public String val(Object x) { return x.toString(); }
				}
				"""
		}
	);
}

// External javadoc references to enum
public void test043() {
	this.runConformTest(
		new String[] {
			"test/E.java",
			"package test;\n" +
				"public enum E { TEST, VALID }\n",
			"test/X.java",
			"""
				import static test.E.TEST;
					/**
					 * @see test.E
					 * @see test.E#VALID
					 * @see #TEST
					 */
				public class X {}
				"""
		}
	);
}
public void test044() {
	this.runConformTest(
		new String[] {
			"test/E.java",
			"package test;\n" +
				"public enum E { TEST, VALID }\n",
			"test/X.java",
			"""
				import static test.E.TEST;
					/**
					 * Valid value = {@value test.E#VALID}
					 * Test value = {@value #TEST}
					 */
				public class X {}
				"""
		}
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78321
 */
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X
				{
				  FIRST,
				  SECOND,
				  THIRD;
				
				  static {
				    for (X t : values()) {
				      System.out.print(t.name());
				    }
				  }
				
				  X() {
				  }
				
				  public static void main(String[] args) {
				  }
				}"""
		},
		"FIRSTSECONDTHIRD"
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78464
 */
public void test046() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				  a(1);
				  X(int i) {
				  }
				}"""
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914
 */
public void test047() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					;
					X() {
						super();
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				super();
				^^^^^^^^
			Cannot invoke super constructor from enum constructor X()
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77211
 */
public void test048() {
	this.runConformTest(
		new String[] {
			"StopLight.java",
			"""
				public enum StopLight{
				    RED{
				        public StopLight next(){ return GREEN; }
				    },
				    GREEN{
				        public StopLight next(){ return YELLOW; }
				    },
				    YELLOW{
				        public StopLight next(){ return RED; }
				    };
				
				   public abstract StopLight next();
				}"""
		},
		""
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78915
 */
public void test049() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public abstract enum X {}"
		},
	"""
		----------
		1. ERROR in X.java (at line 1)
			public abstract enum X {}
			                     ^
		Illegal modifier for the enum X; only public is permitted
		----------
		"""
	);
}

public void test050() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {}"
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
 */
public void test051() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					BLEU (0) {
					}
					;
					X() {
						this(0);
					}
					X(int i) {
					}
				}
				"""
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916
 */
public void test052() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					A
					;
				\t
					public abstract void foo();
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				A
				^
			The enum constant A must implement the abstract method foo()
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test053() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					A () { public void foo() {} }
					;
				\t
					public abstract void foo();
				}
				"""
		},
		""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test054() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					A() {}
					;
				\t
					public abstract void foo();
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				A() {}
				^
			The enum constant A must implement the abstract method foo()
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78916 - variation
 */
public void test055() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					;
				\t
					public abstract void foo();
				}
				"""
		},
	"""
		----------
		1. ERROR in X.java (at line 4)
			public abstract void foo();
			                     ^^^^^
		The enum X can only define the abstract method foo() if it also defines enum constants with corresponding implementations
		----------
		"""
	);
}
// TODO (philippe) enum cannot be declared as local type

// TODO (philippe) check one cannot redefine Enum incorrectly

// TODO (philippe) check enum syntax recovery
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78914 - variation
 */
public void test056() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				    PLUS {
				        double eval(double x, double y) { return x + y; }
				    };
				
				    // Perform the arithmetic X represented by this constant
				    abstract double eval(double x, double y);
				}"""
		},
		""
	);
	String expectedOutput =
		"// Signature: Ljava/lang/Enum<LX;>;\n" +
		"public abstract enum X {\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77430
 */
public void test057() {
	this.runConformTest(
		new String[] {
			"Enum2.java",
			"""
				public class Enum2 {
				    enum Color { RED, GREEN };
				    public static void main(String[] args) {
				        Color c= Color.GREEN;
				        switch (c) {
				        case RED:
				            System.out.println(Color.RED);
				            break;
				        case GREEN:
				            System.out.println(c);
				            break;
				        default: // nop
				        }
				    }
				}
				"""
		},
		"GREEN"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77430 - variation
 */
public void test058() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				enum X { a }
				class A {
					public static void main(String[] args) {
						test(X.a, 9);
						test2(X.a, 3);
					}
					static void test(X x, int a) {
						if (x == a) a++; // incomparable types: X and int
						switch(x) {
							case a : System.out.println(a); // prints \'9\'
				           default: // nop
						}
					}
					static void test2(X x, final int aa) {
						switch(x) {
							case aa : // unqualified enum constant error
								System.out.println(a); // cannot find a
				           default: // nop
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				if (x == a) a++; // incomparable types: X and int
				    ^^^^^^
			Incompatible operand types X and int
			----------
			2. ERROR in X.java (at line 16)
				case aa : // unqualified enum constant error
				     ^^
			aa cannot be resolved or is not a field
			----------
			3. ERROR in X.java (at line 17)
				System.out.println(a); // cannot find a
				                   ^
			a cannot be resolved to a variable
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81262
 */
public void test059() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					MONDAY {
						public void foo() {
						}
					};
					private X() {
					}
					public static void main(String[] args) {
					  System.out.println("SUCCESS");
					}
				}
				"""
		},
		"SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81589
 */
public void test060() {
	this.runNegativeTest(
		new String[] {
			"com/flarion/test/a/MyEnum.java",
			"""
				package com.flarion.test.a;
				public enum MyEnum {
				
				    First, Second;
				   \s
				}
				""",
			"com/flarion/test/b/MyClass.java",
			"""
				package com.flarion.test.b;
				import com.flarion.test.a.MyEnum;
				import static com.flarion.test.a.MyEnum.First;
				import static com.flarion.test.a.MyEnum.Second;
				public class MyClass {
				
				    public void myMethod() {
				        MyEnum e = MyEnum.First;
				        switch (e) {
				        case First:
				            break;
				        case Second:
				            break;
				        default: // nop
				        }
				        throw new Exception();
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in com\\flarion\\test\\b\\MyClass.java (at line 3)
				import static com.flarion.test.a.MyEnum.First;
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The import com.flarion.test.a.MyEnum.First is never used
			----------
			2. WARNING in com\\flarion\\test\\b\\MyClass.java (at line 4)
				import static com.flarion.test.a.MyEnum.Second;
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The import com.flarion.test.a.MyEnum.Second is never used
			----------
			3. ERROR in com\\flarion\\test\\b\\MyClass.java (at line 16)
				throw new Exception();
				^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type Exception
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82217
 */
public void test061() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					A, B, C;
					public static final X D = null;
				}
				
				class A {
					private void foo(X x) {
						switch (x) {
							case D:
						}
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 8)
				switch (x) {
				        ^
			The enum constant A needs a corresponding case label in this enum switch on X
			----------
			2. WARNING in X.java (at line 8)
				switch (x) {
				        ^
			The enum constant B needs a corresponding case label in this enum switch on X
			----------
			3. WARNING in X.java (at line 8)
				switch (x) {
				        ^
			The enum constant C needs a corresponding case label in this enum switch on X
			----------
			4. ERROR in X.java (at line 9)
				case D:
				     ^
			The field X.D cannot be referenced from an enum case label; only enum constants can be used in enum switch
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82217 - variation with qualified name
 */
public void test062() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					A, B, C;
					public static final X D = null;
				}
				
				class A {
					private void foo(X x) {
						switch (x) {
							case X.D:
						}
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 8)
				switch (x) {
				        ^
			The switch over the enum type X should have a default case
			----------
			2. WARNING in X.java (at line 8)
				switch (x) {
				        ^
			The enum constant A needs a corresponding case label in this enum switch on X
			----------
			3. WARNING in X.java (at line 8)
				switch (x) {
				        ^
			The enum constant B needs a corresponding case label in this enum switch on X
			----------
			4. WARNING in X.java (at line 8)
				switch (x) {
				        ^
			The enum constant C needs a corresponding case label in this enum switch on X
			----------
			5. ERROR in X.java (at line 9)
				case X.D:
				       ^
			The field X.D cannot be referenced from an enum case label; only enum constants can be used in enum switch
			----------
			""",
		null, // classlibs
		true, // flush
		options);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=81945
 */
public void test063() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				  enum Option { ALPHA, BRAVO  };
				  void method1(Option item) {
				    switch (item) {
				    case ALPHA:      break;
				    case BRAVO:      break;
				    default:         break;
				    }
				  }
				}
				""",
		},
		"");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82590
 */
public void test064() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X implements B {
				
					C1 {
						public void test() {};
					},
					C2 {
						public void test() {};
					}
				}
				
				interface B {
					public void test();
				\t
				}
				""",
		},
		"");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83847
 */
public void test065() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				  A;
				  private void foo() {
				    X e= new X() {
				    };
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				X e= new X() {
				         ^
			Cannot instantiate the type X
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83860
 */
public void test066() {
    this.runConformTest(
        new String[] {
            "X.java",
            """
				public enum X {
				    SUCCESS (0) {};
				    private X(int i) {}
				    public static void main(String[] args) {
				       for (X x : values()) {
				           System.out.print(x);
				       }
				    }
				}""",
        },
        "SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83219
 */
public void test067() {
    this.runNegativeTest(
        new String[] {
            "X.java",
            """
				public enum X {
				    ONE, TWO, THREE;
				    abstract int getSquare();
				    abstract int getSquare();
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 2)
				ONE, TWO, THREE;
				^^^
			The enum constant ONE must implement the abstract method getSquare()
			----------
			2. ERROR in X.java (at line 2)
				ONE, TWO, THREE;
				     ^^^
			The enum constant TWO must implement the abstract method getSquare()
			----------
			3. ERROR in X.java (at line 2)
				ONE, TWO, THREE;
				          ^^^^^
			The enum constant THREE must implement the abstract method getSquare()
			----------
			4. ERROR in X.java (at line 3)
				abstract int getSquare();
				             ^^^^^^^^^^^
			Duplicate method getSquare() in type X
			----------
			5. ERROR in X.java (at line 4)
				abstract int getSquare();
				             ^^^^^^^^^^^
			Duplicate method getSquare() in type X
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
 */
public void test068() {
    this.runNegativeTest(
        new String[] {
            "X.java",
            """
				public enum X {
				    A(1, 3), B(1, 3), C(1, 3) { }
				   	;
				    public X(int i, int j) { }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 4)
				public X(int i, int j) { }
				       ^^^^^^^^^^^^^^^
			Illegal modifier for the enum constructor; only private is permitted.
			----------
			""");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83648
 */
public void test069() {
    this.runNegativeTest(
        new String[] {
            "X.java",
            """
				public enum X {
				    A(1, 3), B(1, 3), C(1, 3) { }
				   	;
				    protected X(int i, int j) { }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 4)
				protected X(int i, int j) { }
				          ^^^^^^^^^^^^^^^
			Illegal modifier for the enum constructor; only private is permitted.
			----------
			""");
}

public void test070() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				    PLUS {
				        double eval(double x, double y) { return x + y; }
				    };
				
				    // Perform the arithmetic X represented by this constant
				    abstract double eval(double x, double y);
				}"""
		},
		""
	);
	String expectedOutput =
		"""
		  // Method descriptor #18 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X(java.lang.String arg0, int arg1);
		    0  aload_0 [this]
		    1  aload_1 [arg0]
		    2  iload_2 [arg1]
		    3  invokespecial java.lang.Enum(java.lang.String, int) [25]
		    6  return
		""";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test071() {
	this.runConformTest( // no methods to implement
		new String[] {
			"X1.java",
			"""
				public enum X1 implements I {
					;
				}
				interface I {}
				"""
		},
		""
	);
	this.runConformTest( // no methods to implement with constant
		new String[] {
			"X1a.java",
			"""
				public enum X1a implements I {
					A;
				}
				interface I {}
				"""
		},
		""
	);
	this.runConformTest( // no methods to implement with constant body
		new String[] {
			"X1b.java",
			"""
				public enum X1b implements I {
					A() { void random() {} };
				}
				interface I {}
				"""
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test072() {
	this.runConformTest( // implement inherited method
		new String[] {
			"X2.java",
			"""
				public enum X2 implements I {
					;
					public void test() {}
				}
				interface I { void test(); }
				"""
		},
		""
	);
	this.runConformTest( // implement inherited method with constant
		new String[] {
			"X2a.java",
			"""
				public enum X2a implements I {
					A;
					public void test() {}
				}
				interface I { void test(); }
				"""
		},
		""
	);
	this.runConformTest( // implement inherited method with constant body
		new String[] {
			"X2b.java",
			"""
				public enum X2b implements I {
					A() { public void test() {} };
					public void test() {}
				}
				interface I { void test(); }
				"""
		},
		""
	);
	this.runConformTest( // implement inherited method with random constant body
		new String[] {
			"X2c.java",
			"""
				public enum X2c implements I {
					A() { void random() {} };
					public void test() {}
				}
				interface I { void test(); }
				"""
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test073() {
	this.runNegativeTest( // implement inherited method but as abstract
		new String[] {
			"X3.java",
			"""
				public enum X3 implements I {
					;
					public abstract void test();
				}
				interface I { void test(); }
				"""
		},
		"""
			----------
			1. ERROR in X3.java (at line 3)
				public abstract void test();
				                     ^^^^^^
			The enum X3 can only define the abstract method test() if it also defines enum constants with corresponding implementations
			----------
			"""
	);
	this.runNegativeTest( // implement inherited method as abstract with constant
		new String[] {
			"X3a.java",
			"""
				public enum X3a implements I {
					A;
					public abstract void test();
				}
				interface I { void test(); }
				"""
		},
		"""
			----------
			1. ERROR in X3a.java (at line 2)
				A;
				^
			The enum constant A must implement the abstract method test()
			----------
			"""
	);
	this.runConformTest( // implement inherited method as abstract with constant body
		new String[] {
			"X3b.java",
			"""
				public enum X3b implements I {
					A() { public void test() {} };
					public abstract void test();
				}
				interface I { void test(); }
				"""
		},
		""
	);
	this.runNegativeTest( // implement inherited method as abstract with random constant body
		new String[] {
			"X3c.java",
			"""
				public enum X3c implements I {
					A() { void random() {} };
					public abstract void test();
				}
				interface I { void test(); }
				"""
		},
		"""
			----------
			1. ERROR in X3c.java (at line 2)
				A() { void random() {} };
				^
			The enum constant A must implement the abstract method test()
			----------
			"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test074() {
	this.runNegativeTest( // define abstract method
		new String[] {
			"X4.java",
			"""
				public enum X4 {
					;
					public abstract void test();
				}
				"""
		},
		"""
			----------
			1. ERROR in X4.java (at line 3)
				public abstract void test();
				                     ^^^^^^
			The enum X4 can only define the abstract method test() if it also defines enum constants with corresponding implementations
			----------
			"""
	);
	this.runNegativeTest( // define abstract method with constant
		new String[] {
			"X4a.java",
			"""
				public enum X4a {
					A;
					public abstract void test();
				}
				"""
		},
		"""
			----------
			1. ERROR in X4a.java (at line 2)
				A;
				^
			The enum constant A must implement the abstract method test()
			----------
			"""
	);
	this.runConformTest( // define abstract method with constant body
		new String[] {
			"X4b.java",
			"""
				public enum X4b {
					A() { public void test() {} };
					public abstract void test();
				}
				"""
		},
		""
	);
	this.runNegativeTest( // define abstract method with random constant body
		new String[] {
			"X4c.java",
			"""
				public enum X4c {
					A() { void random() {} };
					public abstract void test();
				}
				"""
		},
		"""
			----------
			1. ERROR in X4c.java (at line 2)
				A() { void random() {} };
				^
			The enum constant A must implement the abstract method test()
			----------
			"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83901
public void test075() {
	this.runNegativeTest( // do not implement inherited method
		new String[] {
			"X5.java",
			"""
				public enum X5 implements I {
					;
				}
				interface I { void test(); }
				"""
		},
		"""
			----------
			1. ERROR in X5.java (at line 1)
				public enum X5 implements I {
				            ^^
			The type X5 must implement the inherited abstract method I.test()
			----------
			"""
	);
	this.runNegativeTest( // do not implement inherited method & have constant with no body
		new String[] {
			"X5a.java",
			"""
				public enum X5a implements I {
					A;
				}
				interface I { void test(); }
				"""
		},
		"""
			----------
			1. ERROR in X5a.java (at line 1)
				public enum X5a implements I {
				            ^^^
			The type X5a must implement the inherited abstract method I.test()
			----------
			"""
	);
	this.runConformTest( // do not implement inherited method & have constant with body
		new String[] {
			"X5b.java",
			"""
				public enum X5b implements I {
					A() { public void test() {} };
					;
				}
				interface I { void test(); }
				"""
		},
		""
	);
	this.runNegativeTest( // do not implement inherited method & have constant with random body
		new String[] {
			"X5c.java",
			"""
				public enum X5c implements I {
					A() { void random() {} };
					;
					private X5c() {}
				}
				interface I { void test(); }
				"""
		},
		"""
			----------
			1. ERROR in X5c.java (at line 2)
				A() { void random() {} };
				^
			The enum constant A must implement the abstract method test()
			----------
			"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83902
public void test076() { // bridge method needed
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) { ((I) E.A).foo(); }
				}
				interface I { I foo(); }
				enum E implements I {
					A;
					public E foo() {
						System.out.println("SUCCESS");
						return null;
					}
				}
				"""
		},
		"SUCCESS"
	);
}

public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
						E.A.bar();
					}
				}
				enum E {
					A {
						void bar() {
							new M();
						}
					};
					abstract void bar();
				\t
					class M {
						M() {
							System.out.println("SUCCESS");
						}
					}
				}
				"""
		},
		"SUCCESS"
	);
}

public void test078() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
						E.A.bar();
					}
				}
				enum E {
					A {
						void bar() {
							new X(){
								void baz() {
									new M();
								}
							}.baz();
						}
					};
					abstract void bar();
				\t
					class M {
						M() {
							System.out.println("SUCCESS");
						}
					}
				}
				"""
		},
		"SUCCESS"
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=85397
public void test079() throws Exception {
	String op =
			this.complianceLevel < ClassFileConstants.JDK17 ?
					"""
						----------
						1. ERROR in X.java (at line 3)
							private strictfp X() {}
							                 ^^^
						Illegal modifier for the constructor in type X; only public, protected & private are permitted
						----------
						""" :
					"""
						----------
						1. WARNING in X.java (at line 3)
							private strictfp X() {}
							        ^^^^^^^^
						Floating-point expressions are always strictly evaluated from source level 17. Keyword \'strictfp\' is not required.
						----------
						2. ERROR in X.java (at line 3)
							private strictfp X() {}
							                 ^^^
						Illegal modifier for the constructor in type X; only public, protected & private are permitted
						----------
						""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					A, B;
					private strictfp X() {}
				}
				"""
		},
		op
	);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public strictfp enum X {
					A, B;
					private X() {}
				}
				"""
		},
		""
	);

	String[] expectedOutputs =
			this.complianceLevel < ClassFileConstants.JDK17 ?
					new String[] {
							"  private strictfp X(java.lang.String arg0, int arg1);\n",
							"  public static strictfp X[] values();\n",
							"  public static strictfp X valueOf(java.lang.String arg0);\n"
						} :
			new String[] {
		"  private X(java.lang.String arg0, int arg1);\n",
		"  public static X[] values();\n",
		"  public static X valueOf(java.lang.String arg0);\n"
	};

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	for (int i = 0, max = expectedOutputs.length; i < max; i++) {
		String expectedOutput = expectedOutputs[i];
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=87064
public void test080() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface TestInterface {
					int test();
				}
				
				public enum X implements TestInterface {
					TEST {
						public int test() {
							return 42;
						}
					},
					ENUM {
						public int test() {
							return 37;
						}
					};
				}\s
				"""
		},
		""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87818
public void test081() {
	String expectedErrorMessage = this.complianceLevel < ClassFileConstants.JDK16 ?
			"""
				----------
				1. ERROR in X.java (at line 3)
					enum E {}
					     ^
				The member enum E can only be defined inside a top-level class or interface or in a static context
				----------
				2. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				"""
			:
			"""
				----------
				1. ERROR in X.java (at line 4)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""";
		this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						enum E {}
					    Zork();
					}
				}"""
		},
		expectedErrorMessage);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88223
public void test082() {
	if ( this.complianceLevel < ClassFileConstants.JDK16) {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					class Y {
						enum E {}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				enum E {}
				     ^
			The member enum E must be defined inside a static member type
			----------
			""");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							class Y {
								enum E {}
							}
						}"""
				},
				"");
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static class Y {
						enum E {}
					}
				}"""
		},
		"");
	if ( this.complianceLevel < ClassFileConstants.JDK16) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							void foo() {
								class Local {
									enum E {}
								}
							}
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						enum E {}
						     ^
					The member enum E can only be defined inside a top-level class or interface or in a static context
					----------
					""");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							void foo() {
								class Local {
									enum E {}
								}
							}
						}"""
				},
				"");
	}
}


// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check no emulation warning
public void test083() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					INPUT {
						@Override
						public X getReverse() {
							return OUTPUT;
						}
					},
					OUTPUT {
						@Override
						public X getReverse() {
							return INPUT;
						}
					},
					INOUT {
						@Override
						public X getReverse() {
							return INOUT;
						}
					};
					X(){}
				  Zork z;
					public abstract X getReverse();
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 21)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=87998 - check private constructor generation
public void test084() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					INPUT {
						@Override
						public X getReverse() {
							return OUTPUT;
						}
					},
					OUTPUT {
						@Override
						public X getReverse() {
							return INPUT;
						}
					},
					INOUT {
						@Override
						public X getReverse() {
							return INOUT;
						}
					};
					X(){}
					public abstract X getReverse();
				}
				""",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		  // Method descriptor #20 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X(java.lang.String arg0, int arg1);
		""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88625
public void test085() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Test1 {
						test11, test12
					};
					enum Test2 {
						test21, test22
					};
				
					void foo1(Test1 t1, Test2 t2) {
						boolean b = t1 == t2;
					}
					void foo2(Test1 t1, Object t2) {
						boolean b = t1 == t2;
					}
					void foo3(Test1 t1, Enum t2) {
						boolean b = t1 == t2;
					}
					public static void main(String[] args) {
						boolean booleanTest = (Test1.test11 == Test2.test22);
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				boolean b = t1 == t2;
				            ^^^^^^^^
			Incompatible operand types X.Test1 and X.Test2
			----------
			2. WARNING in X.java (at line 15)
				void foo3(Test1 t1, Enum t2) {
				                    ^^^^
			Enum is a raw type. References to generic type Enum<E> should be parameterized
			----------
			3. ERROR in X.java (at line 19)
				boolean booleanTest = (Test1.test11 == Test2.test22);
				                      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Incompatible operand types X.Test1 and X.Test2
			----------
			""");
}
public void test086() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Test1 {
						V;
						static int foo = 0;
					}
				}
				""",
		},
		"");
}
public void test087() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					enum Test1 {
						V;
						interface Foo {}
					}
				}
				""",
		},
		"");
}
public void test088() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					enum Test1 {
						V;
					}
					Object foo() {
						return this;
					}
				
					static class Sub extends X {
						@Override
						Test1 foo() {
							return Test1.V;
						}
					}
				}
				""",
		},
		"");
}
public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					enum Test1 {
						V;
						protected final Test1 clone() { return V; }
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				protected final Test1 clone() { return V; }
				                      ^^^^^^^
			Cannot override the final method from Enum<X.Test1>
			----------
			2. WARNING in X.java (at line 5)
				protected final Test1 clone() { return V; }
				                      ^^^^^^^
			The method clone() of type X.Test1 should be tagged with @Override since it actually overrides a superclass method
			----------
			""");
}
public void test090() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					enum Test1 {
						V;
						public Test1 foo() { return V; }
					}
					Zork z;
				}
				""",
			"java/lang/Object.java",
			"""
				package java.lang;
				public class Object {
					public Object foo() { return this; }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				public Test1 foo() { return V; }
				             ^^^^^
			The method foo() of type X.Test1 should be tagged with @Override since it actually overrides a superclass method
			----------
			2. ERROR in X.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
public void test091() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					enum Test1 {
						V;
						void foo() {}
					}
					class Member<E extends Test1> {
						void bar(E e) {
							e.foo();
						}
					}
				}
				""",
		},
		"");
}
public void test092() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					enum Test1 {
						V;
						void foo() {}
					}
					class Member<E extends Object & Test1> {
						void bar(E e) {
							e.foo();
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				class Member<E extends Object & Test1> {
				                                ^^^^^
			The type X.Test1 is not an interface; it cannot be specified as a bounded parameter
			----------
			2. ERROR in X.java (at line 9)
				e.foo();
				  ^^^
			The method foo() is undefined for the type E
			----------
			""");
}
// check wildcard can extend Enum superclass
public void test093() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					enum Test1 {
						V;
						void foo() {}
					}
					class Member<E extends Test1> {
						E e;
						void bar(Member<? extends Test1> me) {
						}
					}
				}
				""",
		},
		"");
}
// check super bit is set
public void test094() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"public enum X {\n" +
			"}\n",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		// Signature: Ljava/lang/Enum<LX;>;
		public final enum X {
		 \s
		  // Field descriptor #6 [LX;
		  private static final synthetic X[] ENUM$VALUES;
		 \s
		  // Method descriptor #8 ()V
		  // Stack: 1, Locals: 0
		  static {};
		    0  iconst_0
		    1  anewarray X [1]
		    4  putstatic X.ENUM$VALUES : X[] [10]
		    7  return
		      Line numbers:
		        [pc: 0, line: 1]
		 \s
		  // Method descriptor #15 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X(java.lang.String arg0, int arg1);
		    0  aload_0 [this]
		    1  aload_1 [arg0]
		    2  iload_2 [arg1]
		    3  invokespecial java.lang.Enum(java.lang.String, int) [16]
		    6  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 7] local: this index: 0 type: X
		 \s
		  // Method descriptor #21 ()[LX;
		  // Stack: 5, Locals: 3
		  public static X[] values();
		     0  getstatic X.ENUM$VALUES : X[] [10]
		     3  dup
		     4  astore_0
		     5  iconst_0
		     6  aload_0
		     7  arraylength
		     8  dup
		     9  istore_1
		    10  anewarray X [1]
		    13  dup
		    14  astore_2
		    15  iconst_0
		    16  iload_1
		    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void [22]
		    20  aload_2
		    21  areturn
		      Line numbers:
		        [pc: 0, line: 1]
		 \s
		  // Method descriptor #29 (Ljava/lang/String;)LX;
		  // Stack: 2, Locals: 1
		  public static X valueOf(java.lang.String arg0);
		     0  ldc <Class X> [1]
		     2  aload_0 [arg0]
		     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [30]
		     6  checkcast X [1]
		     9  areturn
		      Line numbers:
		        [pc: 0, line: 1]
		""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
public void test095() { // check missing abstract cases from multiple interfaces
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X implements I, J {\s
					ROUGE;
				}
				interface I { void foo(); }
				interface J { void foo(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public enum X implements I, J {\s
				            ^
			The type X must implement the inherited abstract method J.foo()
			----------
			""");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X implements I, J {\s
					ROUGE;
					public void foo() {}
				}
				interface I { void foo(int i); }
				interface J { void foo(); }
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public enum X implements I, J {\s
				            ^
			The type X must implement the inherited abstract method I.foo(int)
			----------
			""");
}
public void test096() { // check for raw vs. parameterized parameter types
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X implements I {\s
					ROUGE;
					public void foo(A a) {}
				}
				interface I { void foo(A<String> a); }
				class A<T> {}
				"""
		},
		"");
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X implements I {\s
					ROUGE { public void foo(A a) {} }
					;
				}
				interface I { void foo(A<String> a); }
				class A<T> {}
				"""
		},
		"");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X implements I {\s
					ROUGE;
					public void foo(A<String> a) {}
				}
				interface I { void foo(A a); }
				class A<T> {}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public enum X implements I {\s
				            ^
			The type X must implement the inherited abstract method I.foo(A)
			----------
			2. ERROR in X.java (at line 3)
				public void foo(A<String> a) {}
				            ^^^^^^^^^^^^^^^^
			Name clash: The method foo(A<String>) of type X has the same erasure as foo(A) of type I but does not override it
			----------
			3. WARNING in X.java (at line 5)
				interface I { void foo(A a); }
				                       ^
			A is a raw type. References to generic type A<T> should be parameterized
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89982
public void test097() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
				public class E {
					enum Numbers { ONE, TWO, THREE }
					static final String BLANK = "    ";
					void foo(Colors color) {
						switch (color) {
							case BLUE:
							case RED:
								break;
							default: // nop
						}\s
					}
				}
				/**
				 * Enumeration of some basic colors.
				 */
				enum Colors {
					BLACK,
					WHITE,
					RED \s
				}
				""",
		},
		"""
			----------
			1. ERROR in E.java (at line 6)
				case BLUE:
				     ^^^^
			BLUE cannot be resolved or is not a field
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89982 - variation
public void test098() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
				public class E {
					enum Numbers { ONE, TWO, THREE }
					static final String BLANK = "    ";
					void foo(Colors color) {
						switch (color) {
						}\s
					}
				}
				/**
				 * Enumeration of some basic colors.
				 */
				enum Colors {
					BLACK,
					WHITE,
					RED; \s
				  Zork z;
				}
				""",
		},
		"""
			----------
			1. WARNING in E.java (at line 5)
				switch (color) {
				        ^^^^^
			The enum constant BLACK needs a corresponding case label in this enum switch on Colors
			----------
			2. WARNING in E.java (at line 5)
				switch (color) {
				        ^^^^^
			The enum constant RED needs a corresponding case label in this enum switch on Colors
			----------
			3. WARNING in E.java (at line 5)
				switch (color) {
				        ^^^^^
			The enum constant WHITE needs a corresponding case label in this enum switch on Colors
			----------
			4. ERROR in E.java (at line 16)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=89274
public void test099() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class A<T> {
					enum E {
						v1, v2;
					}
				}
				
				public class X extends A<Integer> {
					void a(A.E e) {
						b(e); // no unchecked warning
					}
				
					void b(E e) {
						A<Integer>.E e1 = e;
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				A<Integer>.E e1 = e;
				^^^^^^^^^^^^
			The member type A.E cannot be qualified with a parameterized type, since it is static. Remove arguments from qualifying type A<Integer>
			----------
			""");
}
/* from JLS
"It is a compile-time error to reference a static field of an enum type
that is not a compile-time constant (15.28) from constructors, instance
initializer blocks, or instance variable initializer expressions of that
type.  It is a compile-time error for the constructors, instance initializer
blocks, or instance variable initializer expressions of an enum constant e1
to refer to itself or an enum constant of the same type that is declared to
the right of e1."
	*/
public void test100() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				
					anEnumValue {
						private final X thisOne = anEnumValue;
				
						@Override String getMessage() {
							return "Here is what thisOne gets assigned: " + thisOne;
						}
					};
				
					abstract String getMessage();
				
					public static void main(String[] args) {
						System.out.println(anEnumValue.getMessage());
						System.out.println("SUCCESS");
					}
				
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				private final X thisOne = anEnumValue;
				                          ^^^^^^^^^^^
			Cannot refer to the static enum field X.anEnumValue within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=91761
public void test101() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Foo {
				  public boolean bar();
				}
				enum BugDemo {
				  CONSTANT(new Foo() {
				    public boolean bar() {
				      Zork z;
				      return true;
				    }
				  });
				  BugDemo(Foo foo) {
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90775
public void test102() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				import java.util.*;
				
				public class X <T> {
					enum SomeEnum {
						A, B;
						static SomeEnum foo() {
							return null;
						}
					}
					Enum<SomeEnum> e = SomeEnum.A;
					\t
					Set<SomeEnum> set1 = EnumSet.of(SomeEnum.A);
					Set<SomeEnum> set2 = EnumSet.of(SomeEnum.foo());
				\t
					Foo<Bar> foo = null;
				}
				class Foo <U extends Foo<U>> {
				}
				class Bar extends Foo {
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 15)
				Foo<Bar> foo = null;
				    ^^^
			Bound mismatch: The type Bar is not a valid substitute for the bounded parameter <U extends Foo<U>> of the type Foo<U>
			----------
			2. WARNING in X.java (at line 19)
				class Bar extends Foo {
				                  ^^^
			Foo is a raw type. References to generic type Foo<U> should be parameterized
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93396
public void test103() {
    this.runNegativeTest(
        new String[] {
            "BadEnum.java",
            """
				public class BadEnum {
				  public interface EnumInterface<T extends Object> {
				    public T getMethod();
				  }
				  public enum EnumClass implements EnumInterface<String> {
				    ENUM1 { public String getMethod() { return "ENUM1";} },
				    ENUM2 { public String getMethod() { return "ENUM2";} };
				  }
				}
				}
				""",
        },
        """
			----------
			1. ERROR in BadEnum.java (at line 10)
				}
				^
			Syntax error on token "}", delete this token
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=90215
public void test104() {
    this.runConformTest(
        new String[] {
            "p/Placeholder.java",
			"""
				package p;
				
				public class Placeholder {
				    public static void main(String... argv) {
				        ClassWithBadEnum.EnumClass constant = ClassWithBadEnum.EnumClass.ENUM1;
				        ClassWithBadEnum.main(argv);
					}
				}   \s
				
				""",
            "p/ClassWithBadEnum.java",
			"""
				package p;
				
				public class ClassWithBadEnum {
					public interface EnumInterface<T extends Object> {
					    public T getMethod();
					}
				
					public enum EnumClass implements EnumInterface<String> {
						ENUM1 { public String getMethod() { return "ENUM1";} },
						ENUM2 { public String getMethod() { return "ENUM2";} };
					}
					private EnumClass enumVar;\s
					public EnumClass getEnumVar() {
						return enumVar;
					}
					public void setEnumVar(EnumClass enumVar) {
						this.enumVar = enumVar;
					}
				
					public static void main(String... argv) {
						int a = 1;
						ClassWithBadEnum badEnum = new ClassWithBadEnum();
						badEnum.setEnumVar(ClassWithBadEnum.EnumClass.ENUM1);
						// Should fail if bug manifests itself because there will be two getInternalValue() methods
						// one returning an Object instead of a String
						String s3 = badEnum.getEnumVar().getMethod();
						System.out.println(s3);
					}
				} \s
				""",
        },
        "ENUM1");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void test105() {
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        default: // nop
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black"
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void test106() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        }
							 System.out.print("SUCCESS");
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"SUCCESS",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"SUCCESS",
		null,
		false,
		null,
		null,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void test107() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"BlackBlack",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color { BLACK }"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"BlackBlack",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void test108() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        default:
					            System.out.print("Error");
					            break;
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {RED, GREEN, YELLOW, BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void test109() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
					    public static void main(String[] args) {
							Color c = null;
							 try {
					        	c = BLACK;
							} catch(NoSuchFieldError e) {
								System.out.print("SUCCESS");
								return;
							}
					      	switch(c) {
					       	case BLACK:
					          	System.out.print("Black");
					          	break;
					       	case WHITE:
					          	System.out.print("White");
					          	break;
					      	}
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {RED, GREEN, YELLOW, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"SUCCESS",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void test110() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					public class X {
						public int[] $SWITCH_TABLE$pack$Color;
						public int[] $SWITCH_TABLE$pack$Color() { return null; }
					   public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"Black",
			null,
			true,
			null,
			options,
			null
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"Black",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88395
public void test111() {
	this.runConformTest(
			new String[] {
				"pack/X.java",
				"""
					package pack;
					import static pack.Color.*;
					@SuppressWarnings("incomplete-switch")
					public class X {
						public int[] $SWITCH_TABLE$pack$Color;
						public int[] $SWITCH_TABLE$pack$Color() { return null; }
					   public static void main(String[] args) {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
							 foo();
					    }
					   public static void foo() {
					        Color c = BLACK;
					        switch(c) {
					        case BLACK:
					            System.out.print("Black");
					            break;
					        case WHITE:
					            System.out.print("White");
					            break;
					        }
					    }
					}""",
				"pack/Color.java",
				"package pack;\n" +
				"enum Color {WHITE, BLACK}"
			},
			"BlackBlack"
		);

	this.runConformTest(
		new String[] {
			"pack/Color.java",
			"package pack;\n" +
			"enum Color {BLACK, WHITE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"pack/X.java",
		"BlackBlack",
		null,
		false,
		null,
		null,
		null);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97247
public void test112() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.IGNORE);
	this.runConformTest(
		new String[] {
			"com/annot/Foo.java",
			"""
				package com.annot;
				
				import static com.annot.TestType.*;
				
				public class Foo {
					@Test(type=PERFORMANCE)
					public void testBar() throws Exception {
						Test annotation = this.getClass().getMethod("testBar").getAnnotation(Test.class);
						switch (annotation.type()) {
							case PERFORMANCE:
								System.out.println(PERFORMANCE);
								break;
							case CORRECTNESS:
								System.out.println(CORRECTNESS);
								break;
						}	\t
					}
				}""",
			"com/annot/Test.java",
			"""
				package com.annot;
				
				import static com.annot.TestType.CORRECTNESS;
				import static java.lang.annotation.ElementType.METHOD;
				
				import java.lang.annotation.Target;
				
				@Target(METHOD)
				public @interface Test {
					TestType type() default CORRECTNESS;
				}""",
			"com/annot/TestType.java",
			"""
				package com.annot;
				
				public enum TestType {
					CORRECTNESS,
					PERFORMANCE
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=93789
public void test113() {
	if (this.complianceLevel >= ClassFileConstants.JDK16) {
		return;
	}
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				enum BugDemo {
					FOO() {
						static int bar;
					}
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 3)
				static int bar;
				           ^^^
			The field bar cannot be declared static in a non-static inner type, unless initialized with a constant expression
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99428 and https://bugs.eclipse.org/bugs/show_bug.cgi?id=99655
public void test114() {
    this.runConformTest(
        new String[] {
            "EnumTest.java",
			"""
				import java.lang.reflect.*;
				import java.lang.annotation.*;
				@ExpectedModifiers(Modifier.FINAL)
				public enum EnumTest {
					X(255);
					EnumTest(int r) {}
					public static void main(String argv[]) throws Exception {
						test("EnumTest");
						test("EnumTest$EnumA");
						test("EnumTest$EnumB");
						test("EnumTest$EnumB2");
						test("EnumTest$EnumB3");
						test("EnumTest$EnumC3");
						test("EnumTest$EnumD");
					}
					static void test(String className) throws Exception {
						Class c = Class.forName(className);
						ExpectedModifiers em = (ExpectedModifiers) c.getAnnotation(ExpectedModifiers.class);
						if (em != null) {
							int classModifiers = c.getModifiers();
							int expected = em.value();
							if (expected != (classModifiers & (Modifier.ABSTRACT|Modifier.FINAL|Modifier.STATIC))) {
								if ((expected & Modifier.ABSTRACT) != (classModifiers & Modifier.ABSTRACT))
									System.out.println("FAILED ABSTRACT: " + className);
								if ((expected & Modifier.FINAL) != (classModifiers & Modifier.FINAL))
									System.out.println("FAILED FINAL: " + className);
								if ((expected & Modifier.STATIC) != (classModifiers & Modifier.STATIC))
									System.out.println("FAILED STATIC: " + className);
							}
						}
					}
					@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)
					enum EnumA {
						A;
					}
					@ExpectedModifiers(Modifier.STATIC)
					enum EnumB {
						B {
							int value() { return 1; }
						};
						int value(){ return 0; }
					}
					@ExpectedModifiers(Modifier.STATIC)
					enum EnumB2 {
						B2 {};
						int value(){ return 0; }
					}
					@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)
					enum EnumB3 {
						B3;
						int value(){ return 0; }
					}
					@ExpectedModifiers(Modifier.STATIC)
					enum EnumC implements I {
						C {
							int value() { return 1; }
						};
						int value(){ return 0; }
						public void foo(){}
					}
					@ExpectedModifiers(Modifier.STATIC)
					enum EnumC2 implements I {
						C2 {};
						int value(){ return 0; }
						public void foo(){}
					}
					@ExpectedModifiers(Modifier.FINAL|Modifier.STATIC)
					enum EnumC3 implements I {
						C3;
						int value(){ return 0; }
						public void foo(){}
					}
					@ExpectedModifiers(Modifier.ABSTRACT|Modifier.STATIC)
					enum EnumD {
						D {
							int value() { return 1; }
						};
						abstract int value();
					}
				}
				interface I {
					void foo();
				}
				@Retention(RetentionPolicy.RUNTIME)
				@interface ExpectedModifiers {
					int value();
				}"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713
public void test115() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public enum X {
					VALUE;
				
					static int ASD;
					final static int CST = 0;
				\t
					private X() {
						VALUE = null;
						ASD = 5;
						X.VALUE = null;
						X.ASD = 5;
					\t
						System.out.println(CST);
					}
				}
				""",
        },
		"""
			----------
			1. ERROR in X.java (at line 8)
				VALUE = null;
				^^^^^
			Cannot refer to the static enum field X.VALUE within an initializer
			----------
			2. ERROR in X.java (at line 9)
				ASD = 5;
				^^^
			Cannot refer to the static enum field X.ASD within an initializer
			----------
			3. ERROR in X.java (at line 10)
				X.VALUE = null;
				  ^^^^^
			Cannot refer to the static enum field X.VALUE within an initializer
			----------
			4. ERROR in X.java (at line 11)
				X.ASD = 5;
				  ^^^
			Cannot refer to the static enum field X.ASD within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
public void test116() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
					BLEU,\s
					BLANC,\s
					ROUGE;
					{
						BLEU = null;
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				BLEU = null;
				^^^^
			Cannot refer to the static enum field X.BLEU within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101713 - variation
public void test117() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {\s
					BLEU,\s
					BLANC,\s
					ROUGE;
					{
						X x = BLEU.BLANC; // ko
						X x2 = BLEU; // ko
					}
					static {
						X x = BLEU.BLANC; // ok
						X x2 = BLEU; // ok
					}\t
					X dummy = BLEU; // ko
					static X DUMMY = BLANC; // ok
					X() {
						X x = BLEU.BLANC; // ko
						X x2 = BLEU; // ko
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				X x = BLEU.BLANC; // ko
				      ^^^^
			Cannot refer to the static enum field X.BLEU within an initializer
			----------
			2. ERROR in X.java (at line 6)
				X x = BLEU.BLANC; // ko
				           ^^^^^
			Cannot refer to the static enum field X.BLANC within an initializer
			----------
			3. WARNING in X.java (at line 6)
				X x = BLEU.BLANC; // ko
				           ^^^^^
			The static field X.BLANC should be accessed in a static way
			----------
			4. ERROR in X.java (at line 7)
				X x2 = BLEU; // ko
				       ^^^^
			Cannot refer to the static enum field X.BLEU within an initializer
			----------
			5. WARNING in X.java (at line 10)
				X x = BLEU.BLANC; // ok
				           ^^^^^
			The static field X.BLANC should be accessed in a static way
			----------
			6. ERROR in X.java (at line 13)
				X dummy = BLEU; // ko
				          ^^^^
			Cannot refer to the static enum field X.BLEU within an initializer
			----------
			7. ERROR in X.java (at line 16)
				X x = BLEU.BLANC; // ko
				      ^^^^
			Cannot refer to the static enum field X.BLEU within an initializer
			----------
			8. ERROR in X.java (at line 16)
				X x = BLEU.BLANC; // ko
				           ^^^^^
			Cannot refer to the static enum field X.BLANC within an initializer
			----------
			9. WARNING in X.java (at line 16)
				X x = BLEU.BLANC; // ko
				           ^^^^^
			The static field X.BLANC should be accessed in a static way
			----------
			10. ERROR in X.java (at line 17)
				X x2 = BLEU; // ko
				       ^^^^
			Cannot refer to the static enum field X.BLEU within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102265
public void test118() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public enum X {
						 one,
						 two;
						\s
						 static ArrayList someList;
						\s
						 private X() {
						 		 if (someList == null) {
						 		 		 someList = new ArrayList();
						 		 }
						 }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 7)
				static ArrayList someList;
				       ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			2. ERROR in X.java (at line 10)
				if (someList == null) {
				    ^^^^^^^^
			Cannot refer to the static enum field X.someList within an initializer
			----------
			3. ERROR in X.java (at line 11)
				someList = new ArrayList();
				^^^^^^^^
			Cannot refer to the static enum field X.someList within an initializer
			----------
			4. WARNING in X.java (at line 11)
				someList = new ArrayList();
				               ^^^^^^^^^
			ArrayList is a raw type. References to generic type ArrayList<E> should be parameterized
			----------
			""");
}
public void test119() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					BLEU, BLANC, ROUGE;
					final static int CST = 0;
				    enum Member {
				    	;
				        Object obj1 = CST;
				        Object obj2 = BLEU;
				    }
				}
				"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=102213
public void test120() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				
					A() {
						final X a = A;
						final X a2 = B.A;
						@Override void foo() {
							System.out.println(String.valueOf(a));
							System.out.println(String.valueOf(a2));
						}
					},
					B() {
						@Override void foo(){}
					};
					abstract void foo();
				
					public static void main(String[] args) {
						A.foo();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				final X a = A;
				            ^
			Cannot refer to the static enum field X.A within an initializer
			----------
			2. ERROR in X.java (at line 5)
				final X a2 = B.A;
				             ^
			Cannot refer to the static enum field X.B within an initializer
			----------
			3. ERROR in X.java (at line 5)
				final X a2 = B.A;
				               ^
			Cannot refer to the static enum field X.A within an initializer
			----------
			4. WARNING in X.java (at line 5)
				final X a2 = B.A;
				               ^
			The static field X.A should be accessed in a static way
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=92165
public void test121() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				
					UNKNOWN();
				
					private static String error;
				
					{
						error = "error";
					}
				
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				error = "error";
				^^^^^
			Cannot refer to the static enum field X.error within an initializer
			----------
			""");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=105592
public void test122() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public enum State {
						NORMAL
					}
					public void foo() {
						State state = State.NORMAL;
						switch (state) {
						case (NORMAL) :
							System.out.println(State.NORMAL);
							break;
				       default: // nop
						}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				case (NORMAL) :
				     ^^^^^^^^
			Enum constants cannot be surrounded by parenthesis
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110403
public void test123() {
	this.runNegativeTest(
		new String[] {
			"Foo.java",
			"""
				enum Foo {
				 A(0);
				 Foo(int x) {
				    t[0]=x;
				 }
				 private static final int[] t = new int[12];
				}""",
		},
		"""
			----------
			1. ERROR in Foo.java (at line 4)
				t[0]=x;
				^
			Cannot refer to the static enum field Foo.t within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=1101417
public void test124() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				 public enum X {
				  max {
				   {\s
				     val=3; \s
				   }        \s
				   @Override public String toString() {
				     return Integer.toString(val);
				   }
				  };\s
				  {
				    val=2;
				  }
				  private int val;\s
				  public static void main(String[] args) {
				    System.out.println(max); // 3
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				val=3; \s
				^^^
			Cannot make a static reference to the non-static field val
			----------
			2. ERROR in X.java (at line 7)
				return Integer.toString(val);
				                        ^^^
			Cannot make a static reference to the non-static field val
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=112231
public void test125() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				
				public class X {
					interface I {
						int values();
						enum E implements I {
							A, B, C;
						}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				enum E implements I {
				     ^
			This static method cannot hide the instance method from X.I
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126087
public void test126() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				  public class X {
				    enum NoValues {}
				    public static void main(String[] args) {
				      System.out.println("["+NoValues.values().length+"]");
				    }
				  }
				"""
		},
		"[0]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126087
public void test127() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					VALUE {
						void foo() {
						};
					};
					abstract void foo();
				    public static void main(String[] args) {
				      System.out.println("["+X.values().length+"]");
				    }
				}"""
		},
		"[1]");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"  private static final synthetic X[] ENUM$VALUES;\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}

	disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1.class"));
	actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	expectedOutput =
		"ENUM$VALUES";

	index = actualOutput.indexOf(expectedOutput);
	if (index != -1) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index != -1) {
		assertTrue("Must not have field ENUM$VALUES", false);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127766
public void test128() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE);

	this.runNegativeTest(
         new String[] {
        		 "X.java",
        		 """
					public class X {
						public static void main( String[] args) {
							Enum e = new Enum("foo", 2) {
								public int compareTo( Object o) {
									return 0;
								}
							};
							System.out.println(e);
						}
					}""",
         },
         """
			----------
			1. ERROR in X.java (at line 3)
				Enum e = new Enum("foo", 2) {
				             ^^^^
			The type new Enum(){} may not subclass Enum explicitly
			----------
			""",
         null,
         true,
         options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=141155
public void test129() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				        A, B, C;
				}
				""",
		},
		"");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		// Signature: Ljava/lang/Enum<LX;>;
		public final enum X {
		 \s
		  // Field descriptor #6 LX;
		  public static final enum X A;
		 \s
		  // Field descriptor #6 LX;
		  public static final enum X B;
		 \s
		  // Field descriptor #6 LX;
		  public static final enum X C;
		 \s
		  // Field descriptor #10 [LX;
		  private static final synthetic X[] ENUM$VALUES;
		 \s
		  // Method descriptor #12 ()V
		  // Stack: 4, Locals: 0
		  static {};
		     0  new X [1]
		     3  dup
		     4  ldc <String "A"> [14]
		     6  iconst_0
		     7  invokespecial X(java.lang.String, int) [15]
		    10  putstatic X.A : X [19]
		    13  new X [1]
		    16  dup
		    17  ldc <String "B"> [21]
		    19  iconst_1
		    20  invokespecial X(java.lang.String, int) [15]
		    23  putstatic X.B : X [22]
		    26  new X [1]
		    29  dup
		    30  ldc <String "C"> [24]
		    32  iconst_2
		    33  invokespecial X(java.lang.String, int) [15]
		    36  putstatic X.C : X [25]
		    39  iconst_3
		    40  anewarray X [1]
		    43  dup
		    44  iconst_0
		    45  getstatic X.A : X [19]
		    48  aastore
		    49  dup
		    50  iconst_1
		    51  getstatic X.B : X [22]
		    54  aastore
		    55  dup
		    56  iconst_2
		    57  getstatic X.C : X [25]
		    60  aastore
		    61  putstatic X.ENUM$VALUES : X[] [27]
		    64  return
		      Line numbers:
		        [pc: 0, line: 2]
		        [pc: 39, line: 1]
		 \s
		  // Method descriptor #18 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X(java.lang.String arg0, int arg1);
		    0  aload_0 [this]
		    1  aload_1 [arg0]
		    2  iload_2 [arg1]
		    3  invokespecial java.lang.Enum(java.lang.String, int) [31]
		    6  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 7] local: this index: 0 type: X
		 \s
		  // Method descriptor #34 ()[LX;
		  // Stack: 5, Locals: 3
		  public static X[] values();
		     0  getstatic X.ENUM$VALUES : X[] [27]
		     3  dup
		     4  astore_0
		     5  iconst_0
		     6  aload_0
		     7  arraylength
		     8  dup
		     9  istore_1
		    10  anewarray X [1]
		    13  dup
		    14  astore_2
		    15  iconst_0
		    16  iload_1
		    17  invokestatic java.lang.System.arraycopy(java.lang.Object, int, java.lang.Object, int, int) : void [35]
		    20  aload_2
		    21  areturn
		      Line numbers:
		        [pc: 0, line: 1]
		 \s
		  // Method descriptor #42 (Ljava/lang/String;)LX;
		  // Stack: 2, Locals: 1
		  public static X valueOf(java.lang.String arg0);
		     0  ldc <Class X> [1]
		     2  aload_0 [arg0]
		     3  invokestatic java.lang.Enum.valueOf(java.lang.Class, java.lang.String) : java.lang.Enum [43]
		     6  checkcast X [1]
		     9  areturn
		      Line numbers:
		        [pc: 0, line: 1]
		}""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=141810
public void test130() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					   public static void main(String[] args) {
					      for(Action a : Action.values()) {
					         switch(a) {
					         case ONE:
					            System.out.print("1");
					            break;
					         case TWO:
					            System.out.print("2");
					            break;
					         default:
					            System.out.print("default");
					         }
					      }
					   }
					}""",
				"Action.java",
				"enum Action { ONE, TWO }"
			},
			"12"
		);

	this.runConformTest(
		new String[] {
			"Action.java",
			"enum Action {ONE, TWO, THREE}"
		},
		"",
		null,
		false,
		null
	);

	executeClass(
		"X.java",
		"12default",
		null,
		false,
		null,
		null,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=145732
public void test131() {
	this.runConformTest(
         new String[] {
        		 "X.java",
     			"""
					public enum X {
						//A,B
						;
						public static void main(String[] args) {
							try {
								System.out.println(X.valueOf(null));
							} catch (NullPointerException e) {
								System.out.println("NullPointerException");
							} catch (IllegalArgumentException e) {
								System.out.println("IllegalArgumentException");
							}
						}
					}
					""",
         },
         "NullPointerException");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=145732 - variation
public void test132() {
	this.runConformTest(
         new String[] {
        		 "X.java",
     			"""
					public enum X {
						A,B
						;
						public static void main(String[] args) {
							try {
								System.out.println(X.valueOf(null));
							} catch (NullPointerException e) {
								System.out.println("NullPointerException");
							} catch (IllegalArgumentException e) {
								System.out.println("IllegalArgumentException");
							}
						}
					}
					""",
         },
         "NullPointerException");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=147747
public void test133() throws Exception {
	this.runConformTest(
         new String[] {
        		"X.java",
     			"""
					public enum X {
						A, B, C;
						public static void main(String[] args) {}
					}
					""",
         },
         "");
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		  // Method descriptor #12 ()V
		  // Stack: 4, Locals: 0
		  static {};
		     0  new X [1]
		     3  dup
		     4  ldc <String "A"> [14]
		     6  iconst_0
		     7  invokespecial X(java.lang.String, int) [15]
		    10  putstatic X.A : X [19]
		    13  new X [1]
		    16  dup
		    17  ldc <String "B"> [21]
		    19  iconst_1
		    20  invokespecial X(java.lang.String, int) [15]
		    23  putstatic X.B : X [22]
		    26  new X [1]
		    29  dup
		    30  ldc <String "C"> [24]
		    32  iconst_2
		    33  invokespecial X(java.lang.String, int) [15]
		    36  putstatic X.C : X [25]
		    39  iconst_3
		    40  anewarray X [1]
		    43  dup
		    44  iconst_0
		    45  getstatic X.A : X [19]
		    48  aastore
		    49  dup
		    50  iconst_1
		    51  getstatic X.B : X [22]
		    54  aastore
		    55  dup
		    56  iconst_2
		    57  getstatic X.C : X [25]
		    60  aastore
		    61  putstatic X.ENUM$VALUES : X[] [27]
		    64  return
		      Line numbers:
		        [pc: 0, line: 2]
		        [pc: 39, line: 1]
		""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149042
public void test134() {
    this.runNegativeTest(
        new String[] {
            "X.java",
			"""
				public enum X {
				    INITIAL ,
				    OPENED {
				        {
				            System.out.printf("After the %s constructor\\n",INITIAL);
				        }
				    }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 5)
				System.out.printf("After the %s constructor\\n",INITIAL);
				                                               ^^^^^^^
			Cannot refer to the static enum field X.INITIAL within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149562
// a default case is required to consider that b is initialized (in case E
// takes new values in the future)
public void test135() {
    this.runNegativeTest(
        new String[] {
            "E.java",
			"""
				public enum E {
				    A,
				    B
				}""",
            "X.java",
			"""
				public class X {
				    boolean foo(E e) {
				        boolean b;
				        switch (e) {
				          case A:
				              b = true;
				              break;
				          case B:
				              b = false;
				              break;
				        }
				        return b;
				    }
				}""",
        },
        """
			----------
			1. ERROR in X.java (at line 12)
				return b;
				       ^
			The local variable b may not have been initialized. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=151368
public void test136() {
 this.runConformTest(
     new String[] {
        "X.java",
        """
			import p.BeanName;
			public class X {
				Object o = BeanName.CreateStepApiOperation;
			}""",
		"p/BeanName.java",
		"""
			package p;
			public enum BeanName {
			
			    //~ Enum constants ---------------------------------------------------------
			
			    AbortAllJobsOperation,
			    AbortJobApiOperation,
			    AbortStepOperation,
			    AclVoter,
			    AcquireNamedLockApiOperation,
			    AuthenticationManager,
			    BeginStepOperation,
			    CloneApiOperation,
			    CommanderDao,
			    CommanderServer,
			    ConfigureQuartzOperation,
			    CreateAclEntryApiOperation,
			    CreateActualParameterApiOperation,
			    CreateFormalParameterApiOperation,
			    CreateProcedureApiOperation,
			    CreateProjectApiOperation,
			    CreateResourceApiOperation,
			    CreateScheduleApiOperation,
			    CreateStepApiOperation,
			    DeleteAclEntryApiOperation,
			    DeleteActualParameterApiOperation,
			    DeleteFormalParameterApiOperation,
			    DeleteJobApiOperation,
			    DeleteProcedureApiOperation,
			    DeleteProjectApiOperation,
			    DeletePropertyApiOperation,
			    DeleteResourceApiOperation,
			    DeleteScheduleApiOperation,
			    DeleteStepApiOperation,
			    DispatchApiRequestOperation,
			    DumpStatisticsApiOperation,
			    ExpandJobStepAction,
			    ExportApiOperation,
			    FinishStepOperation,
			    GetAccessApiOperation,
			    GetAclEntryApiOperation,
			    GetActualParameterApiOperation,
			    GetActualParametersApiOperation,
			    GetFormalParameterApiOperation,
			    GetFormalParametersApiOperation,
			    GetJobDetailsApiOperation,
			    GetJobInfoApiOperation,
			    GetJobStatusApiOperation,
			    GetJobStepDetailsApiOperation,
			    GetJobStepStatusApiOperation,
			    GetJobsApiOperation,
			    GetProcedureApiOperation,
			    GetProceduresApiOperation,
			    GetProjectApiOperation,
			    GetProjectsApiOperation,
			    GetPropertiesApiOperation,
			    GetPropertyApiOperation,
			    GetResourceApiOperation,
			    GetResourcesApiOperation,
			    GetResourcesInPoolApiOperation,
			    GetScheduleApiOperation,
			    GetSchedulesApiOperation,
			    GetStepApiOperation,
			    GetStepsApiOperation,
			    GetVersionsApiOperation,
			    GraphWorkflowApiOperation,
			    HibernateFlushListener,
			    ImportApiOperation,
			    IncrementPropertyApiOperation,
			    InvokeCommandOperation,
			    InvokePostProcessorOperation,
			    LoginApiOperation,
			    LogManager,
			    LogMessageApiOperation,
			    ModifyAclEntryApiOperation,
			    ModifyActualParameterApiOperation,
			    ModifyFormalParameterApiOperation,
			    ModifyProcedureApiOperation,
			    ModifyProjectApiOperation,
			    ModifyPropertyApiOperation,
			    ModifyResourceApiOperation,
			    ModifyScheduleApiOperation,
			    ModifyStepApiOperation,
			    MoveStepApiOperation,
			    PauseSchedulerApiOperation,
			    QuartzQueue,
			    QuartzScheduler,
			    ReleaseNamedLockApiOperation,
			    ResourceInvoker,
			    RunProcedureApiOperation,
			    RunQueryApiOperation,
			    SaxReader,
			    ScheduleStepsOperation,
			    SessionCache,
			    SetJobNameApiOperation,
			    SetPropertyApiOperation,
			    SetStepStatusAction,
			    StartWorkflowOperation,
			    StateRefreshOperation,
			    StepCompletionPrecondition,
			    StepOutcomePrecondition,
			    StepScheduler,
			    TemplateOperation,
			    TimeoutWatchdog,
			    UpdateConfigurationOperation,
			    Workspace,
			    XmlRequestHandler;
			
			    //~ Static fields/initializers ---------------------------------------------
			
			    public static final int MAX_BEAN_NAME_LENGTH = 33;
			
			    //~ Methods ----------------------------------------------------------------
			
			    /**
			     * Get this bean name as a property name, i.e. uncapitalized.
			     *
			     * @return String
			     */
			    public String getPropertyName()
			    {
			        return null;
			    }
			}
			""", // =================,
     },
	"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156540
public void test137() {
 this.runConformTest(
     new String[] {
        "X.java",
        """
			public class X {
			
			    interface Interface {
			        public int value();
			    }
			
			    public enum MyEnum implements Interface {
			        ;
			
			        MyEnum(int value) { this.value = value; }       \s
			        public int value() { return this.value; }
			
			        private int value;
			    }
			
			    public static void main(String[] args) {
			        System.out.println(MyEnum.values().length);
			    }
			}""", // =================,
     },
	"0");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156591
public void test138() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					PLUS {
						double eval(double x, double y) {
							return x + y;
						}
					},
					MINUS {
						@Override
						abstract double eval(double x, double y);
					};
					abstract double eval(double x, double y);
				}
				
				""", // =================
		 },
		"""
			----------
			1. WARNING in X.java (at line 3)
				double eval(double x, double y) {
				       ^^^^^^^^^^^^^^^^^^^^^^^^
			The method eval(double, double) of type new X(){} should be tagged with @Override since it actually overrides a superclass method
			----------
			2. ERROR in X.java (at line 7)
				MINUS {
				^^^^^
			The enum constant MINUS cannot define abstract methods
			----------
			3. ERROR in X.java (at line 9)
				abstract double eval(double x, double y);
				                ^^^^^^^^^^^^^^^^^^^^^^^^
			The method eval cannot be abstract in the enum constant MINUS
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=156591 - variation
public void test139() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					PLUS {
						double eval(double x, double y) {
							return x + y;
						}
					},
					MINUS {
						abstract double eval2(double x, double y);
					};
				
					abstract double eval(double x, double y);
				}
				
				""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				double eval(double x, double y) {
				       ^^^^^^^^^^^^^^^^^^^^^^^^
			The method eval(double, double) of type new X(){} should be tagged with @Override since it actually overrides a superclass method
			----------
			2. ERROR in X.java (at line 7)
				MINUS {
				^^^^^
			The enum constant MINUS cannot define abstract methods
			----------
			3. ERROR in X.java (at line 7)
				MINUS {
				^^^^^
			The enum constant MINUS must implement the abstract method eval(double, double)
			----------
			4. ERROR in X.java (at line 8)
				abstract double eval2(double x, double y);
				                ^^^^^^^^^^^^^^^^^^^^^^^^^
			The method eval2 cannot be abstract in the enum constant MINUS
			----------
			"""
	);
}
//check final modifier
public void test140() {
 this.runConformTest(
     new String[] {
    	        "X.java",
    			"""
					public enum X {
						PLUS {/*ANONYMOUS*/}, MINUS;
						void bar(X x) {
							Runnable r = (Runnable)x;
						}
					}""", // =================
     },
	"");
}
//check final modifier
public void test141() {
 this.runNegativeTest(
     new String[] {
    	        "X.java",
    			"""
					public enum X {
						PLUS, MINUS;
						void bar(X x) {
							Runnable r = (Runnable)x;
						}
					}""", // =================
     },
		"""
			----------
			1. ERROR in X.java (at line 4)
				Runnable r = (Runnable)x;
				             ^^^^^^^^^^^
			Cannot cast from X to Runnable
			----------
			""");
}
public void test142() {
 this.runConformTest(
     new String[] {
    	        "X.java",
    			"""
					enum Week {
						Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
					}
					public class X {
						public static void main(String[] args) {
							new X().foo();
							new X().bar();	\t
						}
						void foo() {
							for (Week w : Week.values())
								System.out.print(w + " ");
						}
						void bar() {
							for (Week w : java.util.EnumSet.range(Week.Monday, Week.Friday)) {
								System.out.print(w + " ");
							}
						}
					}
					""", // =================
     },
     "Monday Tuesday Wednesday Thursday Friday Saturday Sunday Monday Tuesday Wednesday Thursday Friday");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=166866
public void test143() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public enum X {
					  A {
					    @Override
					    public String toString() {
					      return a();
					    }
					    public abstract String a();
					  }
					}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				A {
				^
			The enum constant A cannot define abstract methods
			----------
			2. ERROR in X.java (at line 7)
				public abstract String a();
				                       ^^^
			The method a cannot be abstract in the enum constant A
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186822
public void test144() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"public enum X<T> {}",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public enum X<T> {}
				              ^
			Syntax error, enum declaration cannot have type parameters
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=186822
public void test145() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"EnumA.java",
			"""
				public enum EnumA {
				  B1,
				  B2;
				  public void foo(){}
				}""",
			"ClassC.java",
			"""
				public class ClassC {
				  void bar() {
				    EnumA.B1.B1.foo();
				    EnumA.B1.B2.foo();
				  }
				}"""
		},
		// compiler options
		null /* no class libraries */,
		options /* custom options */,
		"""
			----------
			1. ERROR in ClassC.java (at line 3)
				EnumA.B1.B1.foo();
				         ^^
			The static field EnumA.B1 should be accessed in a static way
			----------
			2. ERROR in ClassC.java (at line 4)
				EnumA.B1.B2.foo();
				         ^^
			The static field EnumA.B2 should be accessed in a static way
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207915
public void test146() {
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						enum MyEnum {
							A, B
						}
						final String test;
						public X(MyEnum e) { // error
							switch (e) {
								case A:
									test = "a";
									break;
								case B:
									test = "a";
									break;
								// default: test = "unknown"; // enabling this line fixes above error
							}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				public X(MyEnum e) { // error
				       ^^^^^^^^^^^
			The blank final field test may not have been initialized. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem
			----------
			""");
}
// normal error when other warning is enabled
public void test146b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						enum MyEnum {
							A, B
						}
						final String test;
						public X(MyEnum e) { // error
							switch (e) {
								case A:
									test = "a";
									break;
								case B:
									test = "a";
									break;
								// default: test = "unknown"; // enabling this line fixes above error
							}
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				public X(MyEnum e) { // error
				       ^^^^^^^^^^^
			The blank final field test may not have been initialized
			----------
			2. WARNING in X.java (at line 7)
				switch (e) {
				        ^
			The switch over the enum type X.MyEnum should have a default case
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502
public void test147() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public abstract enum E {
								SUCCESS;
							}
						}
						"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 3)
					public abstract enum E {
					                     ^
				Illegal modifier for the member enum E; only public, protected, private & static are permitted
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	runConformTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"Y.java",
			"""
				import p.X;
				public class Y {
					public static void main(String[] args) {
						System.out.println(X.E.SUCCESS);
					}
				}
				"""
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"null" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone /* javac test options */); // note that Eclipse has errors for X while javac alsore reports for X - no conflict
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test148() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public abstract enum E implements Runnable {
								SUCCESS;
							}
						}
						"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 3)
					public abstract enum E implements Runnable {
					                     ^
				Illegal modifier for the member enum E; only public, protected, private & static are permitted
				----------
				2. ERROR in p\\X.java (at line 3)
					public abstract enum E implements Runnable {
					                     ^
				The type X.E must implement the inherited abstract method Runnable.run()
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);
	runConformTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"Y.java",
			"""
				import p.X;
				public class Y {
					public static void main(String[] args) {
						System.out.println(X.E.SUCCESS);
					}
				}
				"""
		},
		// compiler results
		null /* do not check compiler log */,
		// runtime results
		"null" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone /* javac test options */);// see prev note
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test149() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public enum E implements Runnable {
								SUCCESS;
								public void run(){}
							}
							public static void main(String[] args) {
								Class<E> c = E.class;
								System.out.println(c.getName() + ":" + X.E.SUCCESS);
							}
						}
						"""
			},
			"p.X$E:SUCCESS");

	String expectedOutput =
		"// Signature: Ljava/lang/Enum<Lp/X$E;>;Ljava/lang/Runnable;\n" +
		"public static final enum p.X$E implements java.lang.Runnable {\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"p" + File.separator + "X$E.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test150() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public enum E implements Runnable {
								SUCCESS;
								public void run(){}
							}
							public static void main(String[] args) {
								Class<E> c = E.class;
								System.out.println(c.getName() + ":" + X.E.SUCCESS);
							}
						}
						"""
			},
			"p.X$E:SUCCESS");

	this.runConformTest(
			new String[] {
					"Y.java",
					"""
						import p.X;
						public class Y {
							public static void main(String[] args) {
								System.out.println(X.E.SUCCESS);
							}
						}
						"""
			},
			"SUCCESS",
			null,
			false,
			null);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test151() throws Exception {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public enum E implements Runnable {
								SUCCESS {};
								public void run(){}
							}
							public static void main(String[] args) {
								Class<E> c = E.class;
								System.out.println(c.getName() + ":" + X.E.SUCCESS);
							}
						}
						"""
			},
			"p.X$E:SUCCESS");

	String expectedOutput =
		"// Signature: Ljava/lang/Enum<Lp/X$E;>;Ljava/lang/Runnable;\n" +
		"public abstract static enum p.X$E implements java.lang.Runnable {\n";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"p" + File.separator + "X$E.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227502 - variation
public void test152() {
	this.runConformTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						public class X {
							public enum E implements Runnable {
								SUCCESS {};
								public void run(){}
							}
						}
						"""
			},
			"");
	this.runConformTest(
		new String[] {
				"Y.java",
				"""
					import p.X;
					public class Y {
						public static void main(String[] args) {
							System.out.println(X.E.SUCCESS);
						}
					}
					"""
		},
		"SUCCESS",
		null,
		false,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109
public void test153() {
	this.runNegativeTest(
		new String[] {
				"TestEnum.java",
				"""
					public enum TestEnum {
						RED, GREEN, BLUE;\s
					    static int test = 0; \s
					
					    TestEnum() {
					        TestEnum.test=10;
					    }
					}
					"""
		},
		"""
			----------
			1. ERROR in TestEnum.java (at line 6)
				TestEnum.test=10;
				         ^^^^
			Cannot refer to the static enum field TestEnum.test within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test154() {
	this.runNegativeTest(
		new String[] {
				"TestEnum2.java",
				"""
					public enum TestEnum2 {
						;\s
					   static int test = 0; \s
						TestEnum2() {
					        TestEnum2.test=11;
					   }
					}
					class X {
						static int test = 0;
						X() {
							X.test = 13;
						}
					}
					"""
		},
		"""
			----------
			1. ERROR in TestEnum2.java (at line 5)
				TestEnum2.test=11;
				          ^^^^
			Cannot refer to the static enum field TestEnum2.test within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test155() {
	this.runConformTest(
		new String[] {
				"TestEnum.java",
				"""
					public enum TestEnum {
						RED, GREEN, BLUE;\s
					    static int test = 0; \s
					}
					
					enum TestEnum2 {
						;\s
					    TestEnum2() {
					        TestEnum.test=12;
					    }
					}
					"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test156() {
	this.runConformTest(
		new String[] {
				"TestEnum.java",
				"""
					public enum TestEnum {
						RED, GREEN, BLUE;\s
					    static int test = 0; \s
					
					    TestEnum() {
					        new Object() {
								{ TestEnum.test=10; }
							};
					    }
					}
					"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test157() {
	this.runNegativeTest(
		new String[] {
				"Foo.java",
				"""
					enum Foo {
						ONE, TWO, THREE;
						static int val = 10;
						Foo () {
							this(Foo.val);
							System.out.println(Foo.val);
						}
						Foo(int i){}
						{
							System.out.println(Foo.val);
						}
						int field = Foo.val;
					}
					"""
		},
		"""
			----------
			1. ERROR in Foo.java (at line 5)
				this(Foo.val);
				         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			2. ERROR in Foo.java (at line 6)
				System.out.println(Foo.val);
				                       ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			3. ERROR in Foo.java (at line 10)
				System.out.println(Foo.val);
				                       ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			4. ERROR in Foo.java (at line 12)
				int field = Foo.val;
				                ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test158() {
	this.runNegativeTest(
		new String[] {
				"Foo.java",
				"""
					enum Foo {
						ONE, TWO, THREE;
						static int val = 10;
						Foo () {
							this(val);
							System.out.println(val);
						}
						Foo(int i){}
						{
							System.out.println(val);
						}
						int field = val;
					}
					"""
		},
		"""
			----------
			1. ERROR in Foo.java (at line 5)
				this(val);
				     ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			2. ERROR in Foo.java (at line 6)
				System.out.println(val);
				                   ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			3. ERROR in Foo.java (at line 10)
				System.out.println(val);
				                   ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			4. ERROR in Foo.java (at line 12)
				int field = val;
				            ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test159() {
	this.runNegativeTest(
		new String[] {
				"Foo.java",
				"""
					enum Foo {
						ONE, TWO, THREE;
						static int val = 10;
						Foo () {
							this(get().val);
							System.out.println(get().val);
						}
						Foo(int i){}
						{
							System.out.println(get().val);
						}
						int field = get().val;
						Foo get() { return ONE; }
					}
					"""
		},
		"""
			----------
			1. ERROR in Foo.java (at line 5)
				this(get().val);
				     ^^^
			Cannot refer to an instance method while explicitly invoking a constructor
			----------
			2. WARNING in Foo.java (at line 5)
				this(get().val);
				           ^^^
			The static field Foo.val should be accessed in a static way
			----------
			3. ERROR in Foo.java (at line 5)
				this(get().val);
				           ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			4. WARNING in Foo.java (at line 6)
				System.out.println(get().val);
				                         ^^^
			The static field Foo.val should be accessed in a static way
			----------
			5. ERROR in Foo.java (at line 6)
				System.out.println(get().val);
				                         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			6. WARNING in Foo.java (at line 10)
				System.out.println(get().val);
				                         ^^^
			The static field Foo.val should be accessed in a static way
			----------
			7. ERROR in Foo.java (at line 10)
				System.out.println(get().val);
				                         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			8. WARNING in Foo.java (at line 12)
				int field = get().val;
				                  ^^^
			The static field Foo.val should be accessed in a static way
			----------
			9. ERROR in Foo.java (at line 12)
				int field = get().val;
				                  ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test160() {
	this.runNegativeTest(
		new String[] {
				"Foo.java",
				"""
					enum Foo {
						ONE, TWO, THREE;
						static int val = 10;
						Foo () {
							this(get().val = 1);
							System.out.println(get().val = 2);
						}
						Foo(int i){}
						{
							System.out.println(get().val = 3);
						}
						int field = get().val = 4;
						Foo get() { return ONE; }
					}
					"""
		},
		"""
			----------
			1. ERROR in Foo.java (at line 5)
				this(get().val = 1);
				     ^^^
			Cannot refer to an instance method while explicitly invoking a constructor
			----------
			2. WARNING in Foo.java (at line 5)
				this(get().val = 1);
				           ^^^
			The static field Foo.val should be accessed in a static way
			----------
			3. ERROR in Foo.java (at line 5)
				this(get().val = 1);
				           ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			4. WARNING in Foo.java (at line 6)
				System.out.println(get().val = 2);
				                         ^^^
			The static field Foo.val should be accessed in a static way
			----------
			5. ERROR in Foo.java (at line 6)
				System.out.println(get().val = 2);
				                         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			6. WARNING in Foo.java (at line 10)
				System.out.println(get().val = 3);
				                         ^^^
			The static field Foo.val should be accessed in a static way
			----------
			7. ERROR in Foo.java (at line 10)
				System.out.println(get().val = 3);
				                         ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			8. WARNING in Foo.java (at line 12)
				int field = get().val = 4;
				                  ^^^
			The static field Foo.val should be accessed in a static way
			----------
			9. ERROR in Foo.java (at line 12)
				int field = get().val = 4;
				                  ^^^
			Cannot refer to the static enum field Foo.val within an initializer
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228109 - variation
public void test161() {
	this.runConformTest(
		new String[] {
				"EnumTest1.java",
				"""
					enum EnumTest1 {
						;
						static int foo = EnumTest2.bar;
					}
					enum EnumTest2 {
						;
						static int bar = EnumTest1.foo;
					}
					"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239225
public void test162() {
	this.runNegativeTest(
			new String[] {
				"Status.java", // =================
				"""
					import java.util.HashMap;
					import java.util.Map;
					
					public enum Status {
						GOOD((byte) 0x00), BAD((byte) 0x02);
					
						private static Map<Byte, Status> mapping;
					
						private Status(final byte newValue) {
					
							if (Status.mapping == null) {
								Status.mapping = new HashMap<Byte, Status>();
							}
					
							Status.mapping.put(newValue, this);
						}
					}
					""", // =================
			},
			"""
				----------
				1. ERROR in Status.java (at line 11)
					if (Status.mapping == null) {
					           ^^^^^^^
				Cannot refer to the static enum field Status.mapping within an initializer
				----------
				2. ERROR in Status.java (at line 12)
					Status.mapping = new HashMap<Byte, Status>();
					       ^^^^^^^
				Cannot refer to the static enum field Status.mapping within an initializer
				----------
				3. ERROR in Status.java (at line 15)
					Status.mapping.put(newValue, this);
					       ^^^^^^^
				Cannot refer to the static enum field Status.mapping within an initializer
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239225 - variation
public void test163() {
	this.runConformTest(
			new String[] {
				"Status.java", // =================
				"""
					import java.util.HashMap;
					import java.util.Map;
					
					enum Status {
						GOOD((byte) 0x00), BAD((byte) 0x02);
						private byte value;
						private static Map<Byte, Status> mapping;
						private Status(final byte newValue) {
							this.value = newValue;
						}
						static {
							Status.mapping = new HashMap<Byte, Status>();
							for (Status s : values()) {
								Status.mapping.put(s.value, s);
							}
						}
					}
					""", // =================
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251523
public void test164() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public enum X {
					;
					private X valueOf(String arg0) { return null; }
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				private X valueOf(String arg0) { return null; }
				          ^^^^^^^^^^^^^^^^^^^^
			The enum X already defines the method valueOf(String) implicitly
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251523 - variation
public void test165() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				class Other {
					int dupField;//1
					int dupField;//2
					int dupField;//3
					int dupField;//4
					void dupMethod(int i) {}//5
					void dupMethod(int i) {}//6
					void dupMethod(int i) {}//7
					void dupMethod(int i) {}//8
					void foo() {
						int i = dupMethod(dupField);
					}
				}
				
				public enum X {
				        ;
				        private X valueOf(String arg0) { return null; }//9
				        private X valueOf(String arg0) { return null; }//10
				        private X valueOf(String arg0) { return null; }//11
				        void foo() {
				        	int i = valueOf("");
				        }
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int dupField;//1
				    ^^^^^^^^
			Duplicate field Other.dupField
			----------
			2. ERROR in X.java (at line 3)
				int dupField;//2
				    ^^^^^^^^
			Duplicate field Other.dupField
			----------
			3. ERROR in X.java (at line 4)
				int dupField;//3
				    ^^^^^^^^
			Duplicate field Other.dupField
			----------
			4. ERROR in X.java (at line 5)
				int dupField;//4
				    ^^^^^^^^
			Duplicate field Other.dupField
			----------
			5. ERROR in X.java (at line 6)
				void dupMethod(int i) {}//5
				     ^^^^^^^^^^^^^^^^
			Duplicate method dupMethod(int) in type Other
			----------
			6. ERROR in X.java (at line 7)
				void dupMethod(int i) {}//6
				     ^^^^^^^^^^^^^^^^
			Duplicate method dupMethod(int) in type Other
			----------
			7. ERROR in X.java (at line 8)
				void dupMethod(int i) {}//7
				     ^^^^^^^^^^^^^^^^
			Duplicate method dupMethod(int) in type Other
			----------
			8. ERROR in X.java (at line 9)
				void dupMethod(int i) {}//8
				     ^^^^^^^^^^^^^^^^
			Duplicate method dupMethod(int) in type Other
			----------
			9. ERROR in X.java (at line 11)
				int i = dupMethod(dupField);
				        ^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from void to int
			----------
			10. ERROR in X.java (at line 17)
				private X valueOf(String arg0) { return null; }//9
				          ^^^^^^^^^^^^^^^^^^^^
			The enum X already defines the method valueOf(String) implicitly
			----------
			11. ERROR in X.java (at line 18)
				private X valueOf(String arg0) { return null; }//10
				          ^^^^^^^^^^^^^^^^^^^^
			The enum X already defines the method valueOf(String) implicitly
			----------
			12. ERROR in X.java (at line 19)
				private X valueOf(String arg0) { return null; }//11
				          ^^^^^^^^^^^^^^^^^^^^
			The enum X already defines the method valueOf(String) implicitly
			----------
			13. ERROR in X.java (at line 21)
				int i = valueOf("");
				        ^^^^^^^^^^^
			Type mismatch: cannot convert from X to int
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251814
public void test166() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public enum X {
				        ;
				        private int valueOf(String arg0) { return 0; }//11
				        void foo() {
				        	int i = valueOf("");
				        }
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				private int valueOf(String arg0) { return 0; }//11
				            ^^^^^^^^^^^^^^^^^^^^
			The enum X already defines the method valueOf(String) implicitly
			----------
			2. ERROR in X.java (at line 5)
				int i = valueOf("");
				        ^^^^^^^^^^^
			Type mismatch: cannot convert from X to int
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	// check for presence of #valueOf(...) in problem type
	String expectedOutput =
		"""
		public final enum X {
		 \s
		  // Method descriptor #6 (Ljava/lang/String;I)V
		  // Stack: 3, Locals: 3
		  private X(java.lang.String arg0, int arg1);
		     0  new java.lang.Error [8]
		     3  dup
		     4  ldc <String "Unresolved compilation problems: \\n\\tThe enum X already defines the method valueOf(String) implicitly\\n\\tType mismatch: cannot convert from X to int\\n"> [10]
		     6  invokespecial java.lang.Error(java.lang.String) [12]
		     9  athrow
		      Line numbers:
		        [pc: 0, line: 3]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: X
		 \s
		  // Method descriptor #20 ()V
		  // Stack: 3, Locals: 1
		  void foo();
		     0  new java.lang.Error [8]
		     3  dup
		     4  ldc <String "Unresolved compilation problem: \\n\\tType mismatch: cannot convert from X to int\\n"> [21]
		     6  invokespecial java.lang.Error(java.lang.String) [12]
		     9  athrow
		      Line numbers:
		        [pc: 0, line: 5]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: X
		}""";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=251814 - variation
public void test167() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public enum X {
				    ;
				    static int valueOf(String arg0) { return 0; }//9
				    void foo() {
				    	int i = X.valueOf("");
				    }
				}
				""",
			"Other.java",// =================
			"""
				public class Other {
				    void foo() {
				    	int i = X.valueOf("");
				    }
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				static int valueOf(String arg0) { return 0; }//9
				           ^^^^^^^^^^^^^^^^^^^^
			The enum X already defines the method valueOf(String) implicitly
			----------
			2. ERROR in X.java (at line 5)
				int i = X.valueOf("");
				        ^^^^^^^^^^^^^
			Type mismatch: cannot convert from X to int
			----------
			----------
			1. ERROR in Other.java (at line 3)
				int i = X.valueOf("");
				        ^^^^^^^^^^^^^
			Type mismatch: cannot convert from X to int
			----------
			""",
		null,
		true, // flush output
		null,
		true, // generate output
		false,
		false);
	// check consistency of problem when incremental compiling against X problemType
	this.runNegativeTest(
		new String[] {
				"Other.java",// =================
				"""
					public class Other {
					    void foo() {
					    	int i = X.valueOf("");
					    }
					}
					""", // =================
		},
		"""
			----------
			1. ERROR in Other.java (at line 3)
				int i = X.valueOf("");
				          ^^^^^^^
			The method valueOf(Class<T>, String) in the type Enum<X> is not applicable for the arguments (String)
			----------
			""",
		null,
		false, // flush output
		null,
		true, // generate output
		false,
		false);
	}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255452
public void test168() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				enum BadEnum {
				    CRAZY(CRAZY), // <-- illegal forward reference reported by all compilers
				    IMPOSSIBLE(BadEnum.IMPOSSIBLE); // <-- illegal forward reference (javac 1.6 only)
				    private BadEnum(BadEnum self) {
				    }
				}
				public class X {
				    X x1 = new X(x1);//1 - WRONG
				    static X X2 = new X(X.X2);//2 - OK
				    X x3 = new X(this.x3);//3 - OK
				    X(X x) {}
				    X(int i) {}
				    static int VALUE() { return 13; }
				    int value() { return 14; }
				}
				class Y extends X {
				    X x1 = new X(x1);//6 - WRONG
				    static X X2 = new X(Y.X2);//7 - OK
				    X x3 = new X(this.x3);//8 - OK
				    Y(Y y) { super(y); }
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				CRAZY(CRAZY), // <-- illegal forward reference reported by all compilers
				      ^^^^^
			Cannot reference a field before it is defined
			----------
			2. ERROR in X.java (at line 3)
				IMPOSSIBLE(BadEnum.IMPOSSIBLE); // <-- illegal forward reference (javac 1.6 only)
				                   ^^^^^^^^^^
			Cannot reference a field before it is defined
			----------
			3. ERROR in X.java (at line 8)
				X x1 = new X(x1);//1 - WRONG
				             ^^
			Cannot reference a field before it is defined
			----------
			4. WARNING in X.java (at line 17)
				X x1 = new X(x1);//6 - WRONG
				  ^^
			The field Y.x1 is hiding a field from type X
			----------
			5. ERROR in X.java (at line 17)
				X x1 = new X(x1);//6 - WRONG
				             ^^
			Cannot reference a field before it is defined
			----------
			6. WARNING in X.java (at line 18)
				static X X2 = new X(Y.X2);//7 - OK
				         ^^
			The field Y.X2 is hiding a field from type X
			----------
			7. WARNING in X.java (at line 19)
				X x3 = new X(this.x3);//8 - OK
				  ^^
			The field Y.x3 is hiding a field from type X
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255452 - variation
public void test169() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				enum BadEnum {
				    NOWAY(BadEnum.NOWAY.CONST),
				    INVALID(INVALID.CONST),
				    WRONG(WRONG.VALUE()),
				    ILLEGAL(ILLEGAL.value());
				    final static int CONST = 12;
				    private BadEnum(int i) {
				    }
				    static int VALUE() { return 13; }
				    int value() { return 14; }
				}
				public class X {
				    final static int CONST = 12;
				    X x4 = new X(x4.CONST);//4 - WRONG
				    X x5 = new X(x5.value());//5 - WRONG
				    X(int i) {}
				    static int VALUE() { return 13; }
				    int value() { return 14; }
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				NOWAY(BadEnum.NOWAY.CONST),
				              ^^^^^
			Cannot reference a field before it is defined
			----------
			2. WARNING in X.java (at line 2)
				NOWAY(BadEnum.NOWAY.CONST),
				                    ^^^^^
			The static field BadEnum.CONST should be accessed in a static way
			----------
			3. ERROR in X.java (at line 3)
				INVALID(INVALID.CONST),
				        ^^^^^^^
			Cannot reference a field before it is defined
			----------
			4. WARNING in X.java (at line 3)
				INVALID(INVALID.CONST),
				                ^^^^^
			The static field BadEnum.CONST should be accessed in a static way
			----------
			5. ERROR in X.java (at line 4)
				WRONG(WRONG.VALUE()),
				      ^^^^^
			Cannot reference a field before it is defined
			----------
			6. WARNING in X.java (at line 4)
				WRONG(WRONG.VALUE()),
				      ^^^^^^^^^^^^^
			The static method VALUE() from the type BadEnum should be accessed in a static way
			----------
			7. ERROR in X.java (at line 5)
				ILLEGAL(ILLEGAL.value());
				        ^^^^^^^
			Cannot reference a field before it is defined
			----------
			8. ERROR in X.java (at line 14)
				X x4 = new X(x4.CONST);//4 - WRONG
				             ^^
			Cannot reference a field before it is defined
			----------
			9. WARNING in X.java (at line 14)
				X x4 = new X(x4.CONST);//4 - WRONG
				                ^^^^^
			The static field X.CONST should be accessed in a static way
			----------
			10. ERROR in X.java (at line 15)
				X x5 = new X(x5.value());//5 - WRONG
				             ^^
			Cannot reference a field before it is defined
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=263877
public void test170() {
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				enum Days {
				    Monday("Mon", Days.OFFSET + 0),    // should not complain
				    Tuesday("Tue", Days.Wednesday.hashCode()),   // should complain since enum constant
				    Wednesday("Wed", OFFSET + 2);   // should complain since unqualified
				    public static final int OFFSET = 0;  // cannot move this above, else more errors
				    Days(String abbr, int index) {
				    }
				}
				
				class X {
				    public static final int FOO = X.OFFSET + 0;
				    public static final int BAR = OFFSET + 1;
				    public static final int OFFSET = 0;  // cannot move this above, else more errors
				}
				""", // =================
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Tuesday("Tue", Days.Wednesday.hashCode()),   // should complain since enum constant
				                    ^^^^^^^^^
			Cannot reference a field before it is defined
			----------
			2. ERROR in X.java (at line 4)
				Wednesday("Wed", OFFSET + 2);   // should complain since unqualified
				                 ^^^^^^
			Cannot reference a field before it is defined
			----------
			3. ERROR in X.java (at line 12)
				public static final int BAR = OFFSET + 1;
				                              ^^^^^^
			Cannot reference a field before it is defined
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267670. Make sure we don't emit any unused
// warnings about enumerators. Since these could be used in indirect ways not obvious.
public void test171() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {\s
				    private enum Colors {
					     BLEU,
					     BLANC,
					     ROUGE
					 }
					public static void main(String[] args) {
						for (Colors c: Colors.values()) {
				           System.out.print(c);
						}
					}
				}
				"""
		},
		null, customOptions,
		"",
		"BLEUBLANCROUGE", null,
		JavacTestOptions.DEFAULT);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=267670. Make sure we don't emit any unused
// warnings about enumerators. Since these could be used in indirect ways not obvious. This
// test also verifies that while we don't complain about individual enumerators not being used
// we DO complain if the enumeration type itself is not used.
public void test172() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {\s
				    private enum Greet {
					     HELLO, HOWDY, BONJOUR;\s
				    }
					 private enum Colors {
				        RED, BLACK, BLUE;
				    }
				   private enum Complaint {\
				       WARNING, ERROR, FATAL_ERROR, PANIC;
				   }
					public static void main(String[] args) {
						Greet g = Greet.valueOf("HELLO");
						System.out.print(g);
				       Colors c = Enum.valueOf(Colors.class, "RED");
						System.out.print(c);
				   }
				}
				"""
		},
		null, customOptions,
		"""
			----------
			1. WARNING in X.java (at line 8)
				private enum Complaint {       WARNING, ERROR, FATAL_ERROR, PANIC;
				             ^^^^^^^^^
			The type X.Complaint is never used locally
			----------
			""",
		"HELLORED", null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=273990
public void test173() {
	this.runNegativeTest(
		new String[] {
			"E.java",
			"""
				public enum E {
					A(E.STATIK);
					private static int STATIK = 1;
					private E(final int i) {}
				}
				""",
			"E2.java",
			"""
				public enum E2 {
					A(E2.STATIK);
					static int STATIK = 1;
					private E2(final int i) {}
				}
				"""
		},
		"""
			----------
			1. ERROR in E.java (at line 2)
				A(E.STATIK);
				    ^^^^^^
			Cannot reference a field before it is defined
			----------
			----------
			1. ERROR in E2.java (at line 2)
				A(E2.STATIK);
				     ^^^^^^
			Cannot reference a field before it is defined
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=278562
public void test174() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				interface S {}
				enum A implements S {
					L;
				}
				public class X {
					public static void main(String[] args) throws Exception {
						i(A.L);
					}
					static void i(Enum<? extends S> enumConstant) {
						Map m = new HashMap();
						for (Enum e : enumConstant.getDeclaringClass().getEnumConstants()) {
							m.put(e.name(), e);
						}
						System.out.print(1);
					}
				}
				"""
		},
		"1"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=278562
public void test175() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.HashMap;
				import java.util.Map;
				interface S {}
				enum A implements S {
					L, M, N, O;
				}
				public class X {
					public static void main(String[] args) throws Exception {
						i(new Enum[] {A.L, A.M, A.N, A.O});
					}
					static void i(Enum[] tab) {
						Map m = new HashMap();
						for (Enum s : tab) {
							m.put(s.name(), s);
						}
						System.out.print(1);
					}
				}
				"""
		},
		"1"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test176() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					A(""), B("SUCCESS"), C("Hello");
				\t
					String message;
				\t
					X(@Deprecated String s) {
						this.message = s;
					}
					@Override
					public String toString() {
						return this.message;
					}
				}"""
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"Y.java",
			"""
				public class Y {
					public static void main(String[] args) {
						System.out.println(X.B);
					}
				}"""
		},
		null,
		options,
		"",
		"SUCCESS",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test177() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public enum X {
					A("", 0, "A"), B("SUCCESS", 0, "B"), C("Hello", 0, "C");
				\t
					private String message;
					private int index;
					private String name;
				\t
					X(@Deprecated String s, int i, @Deprecated String name) {
						this.message = s;
						this.index = i;
						this.name = name;
					}
					@Override
					public String toString() {
						return this.message + this.name;
					}
				}"""
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"Y.java",
			"""
				public class Y {
					public static void main(String[] args) {
						System.out.println(X.B);
					}
				}"""
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test178() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static enum Y {
						A("", 0, "A"), B("SUCCESS", 0, "B"), C("Hello", 0, "C");
					\t
						private String message;
						private int index;
						private String name;
						Y(@Deprecated String s, int i, @Deprecated String name) {
							this.message = s;
							this.index = i;
							this.name = name;
						}
						@Override
						public String toString() {
							return this.message + this.name;
						}
					}
				}"""
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"Z.java",
			"""
				public class Z {
					public static void main(String[] args) {
						System.out.println(X.Y.B);
					}
				}"""
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=285701
public void test179() {
	if(this.complianceLevel < ClassFileConstants.JDK1_6) {
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public enum Y {
						A("", 0, "A"), B("SUCCESS", 0, "B"), C("Hello", 0, "C");
					\t
						private String message;
						private int index;
						private String name;
						Y(@Deprecated String s, int i, @Deprecated String name) {
							this.message = s;
							this.index = i;
							this.name = name;
						}
						@Override
						public String toString() {
							return this.message + this.name;
						}
					}
				}"""
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	this.runConformTest(
		false,
		new String[] {
			"Z.java",
			"""
				public class Z {
					public static void main(String[] args) {
						System.out.println(X.Y.B);
					}
				}"""
		},
		null,
		options,
		"",
		"SUCCESSB",
		"",
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=289892
public void test180() {
	this.runConformTest(
		new String[] {
			"p/package-info.java",
			"@p.Annot(state=p.MyEnum.BROKEN)\n" +
			"package p;",
			"p/Annot.java",
			"""
				package p;
				@Annot(state=MyEnum.KO)
				public @interface Annot {
					MyEnum state() default MyEnum.KO;
				}""",
			"p/MyEnum.java",
			"""
				package p;
				@Annot(state=MyEnum.KO)
				public enum MyEnum {
					WORKS, OK, KO, BROKEN, ;
				}""",
			"test180/package-info.java",
			"@p.Annot(state=p.MyEnum.OK)\n" +
			"package test180;",
			"test180/Test.java",
			"""
				package test180;
				import p.MyEnum;
				import p.Annot;
				@Annot(state=MyEnum.OK)
				public class Test {}""",
		},
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	JavacTestOptions.Excuse excuse = JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone;
	if(this.complianceLevel >= ClassFileConstants.JDK16) {
		excuse = null;
	}
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"""
				import test180.Test;
				public class X {
					public static void main(String[] args) {
						System.out.println(Test.class);
					}
				}"""
		},
		null,
		options,
		"",
		"class test180.Test",
		"",
		excuse);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=289892
// in interaction with null annotations
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=365519#c4 item (6)
public void test180a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	this.runConformTest(
		new String[] {
			"p/package-info.java",
			"@p.Annot(state=p.MyEnum.BROKEN)\n" +
			"package p;",
			"p/Annot.java",
			"""
				package p;
				@Annot(state=MyEnum.KO)
				public @interface Annot {
					MyEnum state() default MyEnum.KO;
				}""",
			"p/MyEnum.java",
			"""
				package p;
				@Annot(state=MyEnum.KO)
				public enum MyEnum {
					WORKS, OK, KO, BROKEN, ;
				}""",
			"test180/package-info.java",
			"@p.Annot(state=p.MyEnum.OK)\n" +
			"package test180;",
			"test180/Test.java",
			"""
				package test180;
				import p.MyEnum;
				import p.Annot;
				@Annot(state=MyEnum.OK)
				public class Test {}""",
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
	options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	JavacTestOptions.Excuse excuse = JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone;
	if(this.complianceLevel >= ClassFileConstants.JDK16) {
		excuse = null;
	}
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"""
				import test180.Test;
				public class X {
					public static void main(String[] args) {
						System.out.println(Test.class);
					}
				}"""
		},
		null,
		options,
		"",
		"class test180.Test",
		"",
		excuse);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=300133
public void test181() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public enum X {
						A {
							@Override
							public Object foo(final String s) {
								class Local {
									public String toString() {
										return s;
									}
								}
								return new Local();
							}
						};
						public abstract Object foo(String s);
						public static void main(String... args) {
							 System.out.println(A.foo("SUCCESS"));
						}
					}"""
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test182() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[])   {
						foo();
					}
					public static void foo() {
						int n = 0;
						for (E e : E.values()) {
							if (e.val() == E.VALUES[n++] ) {
								System.out.print(e.val());
							}
						}
					}
				}""",
			"E.java",
			"""
				enum E {
					a1(1), a2(2);
					static int[] VALUES = { 1, 2 };
					private int value;
					E(int v) {
						this.value = v;
					}
					public int val() {
						return this.value;
					}
				}"""
		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test183() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
					}
					static {
						int n = 0;
						for (E e : E.values()) {
							if (e.val() == E.VALUES[n++] ) {
								System.out.print(e.val());
							}
						}
					}
				}""",
			"E.java",
			"""
				enum E {
					a1(1), a2(2);
					static int[] VALUES = { 1, 2 };
					private int value;
					E(int v) {
						this.value = v;
					}
					public int val() {
						return this.value;
					}
				}"""
		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328519
public void test184() throws Exception {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
						new X();
					}
					X() {
						int n = 0;
						for (E e : E.values()) {
							if (e.val() == E.VALUES[n++] ) {
								System.out.print(e.val());
							}
						}
					}
				}""",
			"E.java",
			"""
				enum E {
					a1(1), a2(2);
					static int[] VALUES = { 1, 2 };
					private int value;
					E(int v) {
						this.value = v;
					}
					public int val() {
						return this.value;
					}
				}"""
		},
		"12",
		null/*classLibraries*/,
		true/*shouldFlushOutputDirectory*/,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=
public void test185() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X {
				  A, B;
				  private X() throws Exception {
				  }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				A, B;
				^
			Unhandled exception type Exception
			----------
			2. ERROR in X.java (at line 2)
				A, B;
				   ^
			Unhandled exception type Exception
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test186() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				enum X {
				  A, B;
				}
				public class Y {
				    void test(X x) {
				        switch (x) {
							case A: System.out.println("A"); break;
				 			default : System.out.println("unknown"); break;
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in Y.java (at line 6)
				switch (x) {
				        ^
			The enum constant B should have a corresponding case label in this enum switch on X. To suppress this problem, add a comment //$CASES-OMITTED$ on the line above the 'default:'
			----------
			""",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	options.put(JavaCore.COMPILER_PB_INCOMPLETE_ENUM_SWITCH, JavaCore.ERROR);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				enum X {
				  A, B;
				}
				public class Y {
				    void test(X x) {
				        switch (x) {
							case A: System.out.println("A");
				           //$FALL-THROUGH$
				           //$CASES-OMITTED$
				 			default : System.out.println("unknown"); break;
				        }
				    }
				}
				""",
		},
		"",
		null, // classlibs
		true, // flush
		null, // vmArgs
		options,
		null /*requestor*/);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_ENUM_CASE_DESPITE_DEFAULT, JavaCore.ENABLED);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				enum X {
				  A, B;
				}
				public class Y {
				    void test(X x) {
				        switch (x) {
							case A: System.out.println("A"); break;
				           //$CASES-OMITTED$
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in Y.java (at line 6)
				switch (x) {
				        ^
			The enum constant B needs a corresponding case label in this enum switch on X
			----------
			""",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test187b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.ERROR);
	options.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				enum X {
				  A, B;
				}
				public class Y {
				    @SuppressWarnings("incomplete-switch")
				    void test(X x) {
				        switch (x) {
							case A: System.out.println("A"); break;
				        }
				    }
				}
				""",
		},
		"",
		null, // classlibs
		true, // flush
		null, // vmArgs
		options,
		null /*requestor*/);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test188() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SWITCH_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				enum X {
				  A, B;
				}
				public class Y {
				    void test(X x) {
				        switch (x) {
							case A: System.out.println("A"); break;
							case B: System.out.println("B"); break;
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. WARNING in Y.java (at line 6)
				switch (x) {
				        ^
			The switch over the enum type X should have a default case
			----------
			""",
		null, // classlibs
		true, // flush
		options, // customOptions
		false /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false /* do not skip javac for this peculiar test */,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings,
		false /* performStatementsRecovery */
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
public void test189() {
	Map options = getCompilerOptions();
	//options.put(JavaCore.COMPILER_PB_MISSING_DEFAULT_CASE, JavaCore.WARNING);
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				enum X {
				  A, B;
				}
				public class Y {
				    int test(X x) {
				        switch (x) {
							case A: return 1;
							case B: return 2;
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in Y.java (at line 5)
				int test(X x) {
				    ^^^^^^^^^
			This method must return a result of type int. Note that a problem regarding missing 'default:' on 'switch' has been suppressed, which is perhaps related to this problem
			----------
			""",
		null, // classlibs
		true, // flush
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433060 [1.8][compiler] enum E<T>{I;} causes NPE in AllocationExpression.checkTypeArgumentRedundancy
public void test433060() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_REDUNDANT_TYPE_ARGUMENTS, JavaCore.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public enum X<T> {
					OBJ;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public enum X<T> {
				              ^
			Syntax error, enum declaration cannot have type parameters
			----------
			""",
		null,
		true,
		options);
}
public void test434442() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	this.runConformTest(new String[] {
			"X.java",
			"""
				interface I {
					public enum Letter {
				  		A, B;
					}
				  public default void test(Letter letter) {
				    switch (letter) {
				      case A:
				        System.out.println("A");
				        break;
				      case B:
				        System.out.println("B");
				        break;
				    }
				  }
				}
				
				public class X implements I {
				  public static void main(String[] args) {
					  try{
						  X x = new X();
						  x.test(Letter.A);
					  }
				    catch (Exception e) {
				      e.printStackTrace();
				    }
				  }
				}\s
				
				"""
	});
}
public void test476281() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	this.runConformTest(new String[] {
			"LambdaEnumLocalClassBug.java",
			"""
				public enum LambdaEnumLocalClassBug {
				  A(() -> {
				    class Foo {
				    }
				    new Foo();
				    System.out.println("Success");
				  })
				;
				  private final Runnable runnable;
				  private LambdaEnumLocalClassBug(Runnable runnable) {
				    this.runnable = runnable;
				  }
				  public static void main(String[] args) {
				    A.runnable.run();
				  }
				}"""},
			"Success");
}
public void test476281a() {
	this.runConformTest(new String[] {
			"Test.java",
			"""
				public enum Test {
				  B(new Runnable() {
					public void run() {
						//
						class Foo {
						\t
						}
						new Foo();
				    System.out.println("Success");
					}
				});
				  private final Runnable runnable;
				  private Test(Runnable runnable) {
				    this.runnable = runnable;
				  }
				  public static void main(String[] args) {
				    B.runnable.run();
				  }
				}"""},
			"Success");
}
public void testBug388314() throws Exception {
	this.runConformTest(
			new String[] {
					"p/Nullable.java",
					"""
						package p;
						import static java.lang.annotation.ElementType.*;
						import java.lang.annotation.*;
						@Documented
						@Retention(RetentionPolicy.RUNTIME)
						@Target(value = { FIELD, LOCAL_VARIABLE, METHOD, PARAMETER })
						public @interface Nullable {
							// Nothing to do.
						}""",
					"p/EnumWithNullable.java",
					"""
						package p;
						public enum EnumWithNullable {
							A;
						
							@Nullable
							private final Object b;
						
							private EnumWithNullable(@Nullable Object b) {
								this.b = b;
							}
						
							private EnumWithNullable() {
								this(null);
							}
						}
						"""
			},
			"");

	String expectedOutput =
		"""
		  // Method descriptor #27 (Ljava/lang/String;ILjava/lang/Object;)V
		  // Stack: 3, Locals: 4
		  private EnumWithNullable(java.lang.String arg0,  int arg1, @p.Nullable java.lang.Object b);
		""";

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"p" + File.separator + "EnumWithNullable.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
}
