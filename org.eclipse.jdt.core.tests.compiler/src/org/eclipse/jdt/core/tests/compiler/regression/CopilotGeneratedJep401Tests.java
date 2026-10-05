/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Copilot - specification-derived regression tests for JEP 401
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.PreviewTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Regression tests for JEP 401: Value Classes and Objects (Java 28 preview)
 *
 * These tests are derived from the JLS 28 specification changes, specifically
 * focusing on early construction context (ECC) rules for field access via
 * simple names vs. qualified 'this' and 'ClassName.this' access patterns.
 *
 * Test coverage: JLS sections 3.8/3.9, 6.5.6.1, 8.1.1.5, 8.3, 8.8.7, 14.19, 15.8.3/15.8.4, 16
 */
@RunJavac
@PreviewTest
public class CopilotGeneratedJep401Tests extends AbstractRegressionTestCommon {

	static {
		//		TESTS_NUMBERS = new int [] { 1 };
		//		TESTS_RANGE = new int[] { 1, -1 };
		//		TESTS_NAMES = new String[] { "testECC_QualifiedThisFieldRead_Negative" };
	}

	public static Class<?> testClass() {
		return CopilotGeneratedJep401Tests.class;
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_28);
	}

	public CopilotGeneratedJep401Tests(String testName) {
		super(testName);
	}

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 28");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}

	@Override
	protected JavacTestOptions getJavacTestOptions() {
		return JAVAC_OPTIONS;
	}

	// =================================================================
	// JLS 6.5.6.1: Simple Name Reference in Early Construction Context
	// =================================================================

	/**
	 * JLS 6.5.6.1 (JEP 401 change): Simple name field reference allowed in ECC
	 * for fields declared in the current class only.
	 * Positive: Field declared in current value class, simple-name reference in prologue.
	 */
	public void testECC_SimpleNameFieldRead_Conform_ValueClass() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 10;
				X() {
					int copy = f;  // simple name, allowed
					System.out.println(copy);
					super();
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
		}, "10");
	}

	/**
	 * JLS 6.5.6.1: Simple-name read of blank field in prologue, then assign.
	 */
	public void testECC_SimpleNameBlankFieldAssignThenRead_Conform() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 42;
					int copy = f;  // read after assignment, allowed
					System.out.println(copy);
					super();
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
		}, "42");
	}

	/**
	 * JLS 6.5.6.1: Simple-name read of blank field before assignment is forbidden.
	 */
	public void testECC_SimpleNameBlankFieldReadBeforeAssign_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					int copy = f;  // read before assignment, forbidden
					f = 42;
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	int copy = f;  // read before assignment, forbidden\n" +
		"	           ^\n" +
		"The blank final field f may not have been initialized\n" +
		"----------\n");
	}

	/**
	 * JLS 6.5.6.1: Simple-name reference to inherited field is forbidden in ECC.
	 */
	public void testECC_SimpleNameInheritedFieldRead_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			class Super {
				int inherited = 5;
			}

			public class X extends Super {
				int own;
				X() {
					int a = inherited;  // inherited field, forbidden
					int b = own;        // own field, allowed
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	int a = inherited;  // inherited field, forbidden\n" +
		"	        ^^^^^^^^^\n" +
		"Cannot refer to field inherited in an early construction context\n" +
		"----------\n");
	}

	// =================================================================
	// JLS 15.8.3/15.8.4: Qualified 'this' in Early Construction Context
	// =================================================================

	/**
	 * JLS 15.8.3/15.8.4 (JEP 401 change): Qualified 'this.field' reads forbidden in ECC,
	 * even after assignment.
	 */
	public void testECC_QualifiedThisFieldRead_Negative_AfterAssignment() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				int x;
				X() {
					x = 42;
					int y = this.x;  // qualified this.field, forbidden
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	int y = this.x;  // qualified this.field, forbidden\n" +
		"	        ^^^^^^\n" +
		"Cannot refer to field x in an early construction context\n" +
		"----------\n");
	}

	/**
	 * JLS 15.8.3/15.8.4: Qualified 'this.field' write (LHS assignment) forbidden in ECC.
	 */
	public void testECC_QualifiedThisFieldWrite_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X() {
					this.f += 2;  // this-qualified assignment, forbidden
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	this.f += 2;  // this-qualified assignment, forbidden\n" +
		"	^^^^^^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n");
	}

	/**
	 * JLS 15.8.4: Qualified enclosing-instance 'ClassName.this' reads forbidden in ECC.
	 */
	public void testECC_EnclosingInstanceQualifiedThis_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

				class Inner {
					int f;
					Inner() {
						int a = X.Inner.this.f;  // qualified ClassName.this, forbidden
						super();
					}
				}

				void test() {
					new Inner();
				}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	int a = X.Inner.this.f;  // qualified ClassName.this, forbidden\n" +
		"	        ^^^^^^^^^^^^^^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n");
	}

	// =================================================================
	// JLS 6.5.6.1: Simple-name Assignment in Prologue (LHS)
	// =================================================================

	/**
	 * JLS 6.5.6.1: Simple-name write to blank field in prologue allowed.
	 */
	public void testECC_SimpleNameBlankFieldAssign_Conform() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 99;  // simple-name assignment to blank field, allowed
					System.out.println(f);
					super();
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
		}, "99");
	}

	/**
	 * JLS 6.5.6.1 (JEP 401 change): Simple-name write to initialized field forbidden in prologue.
	 */
	public void testECC_SimpleNameInitializedFieldAssign_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 10;
				X() {
					f = 20;  // simple-name assignment to initialized field, forbidden
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	f = 20;  // simple-name assignment to initialized field, forbidden\n" +
		"	^\n" +
		"Cannot assign field 'f' in an early construction context, because it has an initializer\n" +
		"----------\n");
	}

	/**
	 * JLS 6.5.6.1: Double assignment to same blank field in prologue forbidden.
	 */
	public void testECC_BlankFieldDoubleAssign_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 1;
					f = 2;  // second assignment to blank field, forbidden
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	f = 2;  // second assignment to blank field, forbidden\n" +
		"	^\n" +
		"The final field f may already have been assigned\n" +
		"----------\n");
	}

	// =================================================================
	// JLS 8.8.7: Constructor Delegation (this(..) and super(..))
	// =================================================================

	/**
	 * JLS 8.8.7: this(..) delegation in prologue forbids field reads and writes.
	 */
	public void testECC_AlternateConstructorCall_FieldAccessForbidden_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 1;       // assignment in prologue before this(..), forbidden
					this(2);
				}
				X(int i) {
					f = i;
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	f = 1;       // assignment in prologue before this(..), forbidden\n" +
		"	^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n");
	}

	/**
	 * JLS 8.8.7: this(..) call with field reference in argument forbidden.
	 */
	public void testECC_AlternateConstructorCallWithFieldArg_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X() {
					this(f);  // field reference in this(..) argument, forbidden
				}
				X(int i) {
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	this(f);  // field reference in this(..) argument, forbidden\n" +
		"	     ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n");
	}

	// =================================================================
	// JLS 16: Definite Assignment & Blank Fields
	// =================================================================

	/**
	 * JLS 16: Blank field must be assigned before super() in value class constructor.
	 */
	public void testECC_BlankFieldNotInitializedBeforeSuper_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					super();  // blank field f not assigned
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 4)\n" +
		"	super();  // blank field f not assigned\n" +
		"	^^^^^^^^\n" +
		"The field 'f' must be initialized before chaining to the super class constructor\n" +
		"----------\n");
	}

	/**
	 * JLS 16: Blank field assigned conditionally is not definitely assigned before super().
	 */
	public void testECC_ConditionalAssignmentNotDefinite_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X(boolean b) {
					if (b)
						f = 1;
					super();  // f not definitely assigned
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	super();  // f not definitely assigned\n" +
		"	^^^^^^^^\n" +
		"The field 'f' must be initialized before chaining to the super class constructor\n" +
		"----------\n");
	}

	// =================================================================
	// JLS 14.19: Synchronization on Value Class Types
	// =================================================================

	/**
	 * JLS 14.19 (JEP 401 change): Compile-time rejection of synchronize on final value class.
	 */
	public void testSync_FinalValueClassDirect_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			value class FinalValue {
				int x = 42;
			}

			public class X {
				public static void main(String[] args) {
					FinalValue fv = new FinalValue();
					synchronized (fv) {  // compile-time error
						System.out.println("unreachable");
					}
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	value class FinalValue {\n" +
		"	^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	synchronized (fv) {  // compile-time error\n" +
		"	              ^^\n" +
		"Illegal attempt to synchronize on an instance of a value class\n" +
		"----------\n");
	}

	/**
	 * JLS 14.19: Type variable bounded by final value class forbidden to synchronize.
	 */
	public void testSync_TypeVarBoundedByFinalValue_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			value class FinalValue {}

			public class X {
				<T extends FinalValue> void test(T t) {
					synchronized (t) {  // compile-time error
						System.out.println("unreachable");
					}
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	value class FinalValue {}\n" +
		"	^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 4)\n" +
		"	<T extends FinalValue> void test(T t) {\n" +
		"	           ^^^^^^^^^^\n" +
		"The type parameter T should not be bounded by the final type FinalValue. Final types cannot be further extended\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 5)\n" +
		"	synchronized (t) {  // compile-time error\n" +
		"	              ^\n" +
		"Illegal attempt to synchronize on an instance of a value class\n" +
		"----------\n");
	}

	/**
	 * JLS 14.19: Synchronize on abstract value class reference compiles, throws at runtime.
	 */
	public void testSync_AbstractValueClassReference_RuntimeException_Conform() {
		runConformTest(new String[] {
			"X.java",
			"""
			abstract value class AbstractValue {}
			value class ConcreteValue extends AbstractValue {}

			public class X {
				public static void main(String[] args) {
					AbstractValue av = new ConcreteValue();
					try {
						synchronized (av) {
							System.out.println("unreachable");
						}
					} catch (IdentityException e) {
						System.out.println("caught");
					}
				}
			}
			"""
		}, "caught");
	}

	// =================================================================
	// JLS 8.3: Field Finality in Value Classes
	// =================================================================

	/**
	 * JLS 8.3: All instance fields in value class are implicitly final.
	 */
	public void testFieldFinality_ImplicitlyFinal_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 42;
					super();
				}
				void foo() {
					f = 100;  // illegal: attempt to modify final field
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	f = 100;  // illegal: attempt to modify final field\n" +
		"	^\n" +
		"The final field X.f cannot be assigned\n" +
		"----------\n");
	}

	// =================================================================
	// JLS 3.8/3.9: 'value' as Restricted Identifier
	// =================================================================

	/**
	 * JLS 3.8/3.9: 'value' is a restricted identifier, not a valid type name.
	 */
	public void testRestrictedIdentifier_ValueAsTypeName_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				public static void main(String[] args) {
					System.out.println("test");
				}
			}
			class value {}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	class value {}\n" +
		"	      ^^^^^\n" +
		"'value' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 28\n" +
		"----------\n");
	}

	/**
	 * JLS 3.8/3.9: 'value' is allowed as class modifier.
	 */
	public void testRestrictedIdentifier_ValueAsModifier_Conform() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				public static void main(String[] args) {
					System.out.println("ok");
				}
			}
			"""
		}, "ok");
	}

	// =================================================================
	// JLS 8.1.1.5: Value Class Restrictions
	// =================================================================

	/**
	 * JLS 8.1.1.5: Value class cannot subclass concrete identity class.
	 */
	public void testValueClassSubclassing_CannotSubclassConcreteIdentity_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			class ConcreteIdentity {}

			value class V extends ConcreteIdentity {}  // illegal
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	value class V extends ConcreteIdentity {}  // illegal\n" +
		"	^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	value class V extends ConcreteIdentity {}  // illegal\n" +
		"	                      ^^^^^^^^^^^^^^^^\n" +
		"A value class may extend either java.lang.Object or an abstract value class, but not an identity class\n" +
		"----------\n");
	}

	/**
	 * JLS 8.1.1.5: Abstract value class can extend Object or abstract value class.
	 */
	public void testValueClassSubclassing_AbstractExtendingAbstractValue_Conform() {
		runConformTest(new String[] {
			"Derived.java",
			"""
			abstract value class Base {
				Base() {
					System.out.println("base");
				}
			}

			public value class Derived extends Base {
				Derived() {
					super();
					System.out.println("derived");
				}
				public static void main(String[] args) {
					new Derived();
				}
			}
			"""
		}, "base\nderived");
	}

	/**
	 * JLS 8.1.1.5: Identity class cannot subclass concrete value class.
	 */
	public void testValueClassSubclassing_IdentityCannotSubclassConcrete_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			value class ConcreteValue {}

			class Identity extends ConcreteValue {}  // illegal
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	value class ConcreteValue {}\n" +
		"	^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 3)\n" +
		"	class Identity extends ConcreteValue {}  // illegal\n" +
		"	                       ^^^^^^^^^^^^^\n" +
		"The type Identity cannot subclass the final class ConcreteValue\n" +
		"----------\n");
	}

	/**
	 * JLS 8.1.1.5: Identity class can subclass abstract value class.
	 */
	public void testValueClassSubclassing_IdentityExtendsAbstractValue_Conform() {
		runConformTest(new String[] {
			"Identity.java",
			"""
			abstract value class AbstractValue {}

			public class Identity extends AbstractValue {
				Identity() {
					super();
					System.out.println("ok");
				}
				public static void main(String[] args) {
					new Identity();
				}
			}
			"""
		}, "ok");
	}

	// =================================================================
	// JLS 8.1.1.5: Synchronized Instance Methods in Value Classes
	// =================================================================

	/**
	 * JLS 8.1.1.5: Value class cannot declare synchronized instance methods.
	 */
	public void testSynchronizedInstanceMethod_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				synchronized void foo() {}  // illegal
				static synchronized void bar() {}  // ok
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	synchronized void foo() {}  // illegal\n" +
		"	                  ^^^^^\n" +
		"A value class may not declare a synchronized instance method\n" +
		"----------\n");
	}

	// =================================================================
	// Lambdas and Nested Classes: Field Access in ECC
	// =================================================================

	/**
	 * JLS: Lambda in prologue cannot read fields in ECC.
	 */
	public void testECC_LambdaInPrologue_FieldRead_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 1;
					Runnable r = () -> System.out.println(f);  // lambda reads field in ECC
					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	Runnable r = () -> System.out.println(f);  // lambda reads field in ECC\n" +
		"	                                      ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n");
	}

	/**
	 * JLS: Local class in prologue cannot read fields in ECC.
	 */
	public void testECC_LocalClassInPrologue_FieldRead_Negative() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				int f;
				X() {
					int early = f;  // force proxy

					class L {
						L() {
							int e = f;  // local class reads field in ECC
						}
					}

					super();
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 6)\n" +
		"	class L {\n" +
		"	      ^\n" +
		"The type L is never used locally\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	int e = f;  // local class reads field in ECC\n" +
		"	        ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n");
	}

	// =================================================================
	// Parameter Shadowing
	// =================================================================

	/**
	 * JLS 6.5.6.1: Parameter shadows field; simple name binds to parameter.
	 */
	public void testECC_ParameterShadowsField_BindsToParameter_Conform() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X(int f) {
					System.out.println(f);  // binds to parameter, not field
					super();
				}
				public static void main(String[] args) {
					new X(2);
				}
			}
			"""
		}, "2");
	}

}