/*******************************************************************************
 * Copyright (c) 2010, 2022 GK Software AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *     Till Brychcy <register.eclipse@brychcy.de> - Contribution for
 *								Bug 415413 - [compiler][null] NullpointerException in Null Analysis caused by interaction of LoopingFlowContext and FinallyFlowContext
 *								Bug 415269 - [compiler][null] NonNullByDefault is not always inherited to nested classes
 *     IBM Corporation - additional tests
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

// see bug 186342 - [compiler][null] Using annotations for null checking
@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullAnnotationTest extends AbstractNullAnnotationTest {

private String TEST_JAR_SUFFIX = ".jar";

public NullAnnotationTest(String name) {
	super(name);
}

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "testBug545715" };
//		TESTS_NUMBERS = new int[] { 561 };
//		TESTS_RANGE = new int[] { 1, 2049 };
}

public static Test suite() {
	return buildComparableTestSuite(testClass());
}

public static Class testClass() {
	return NullAnnotationTest.class;
}

String mismatch_NonNull_Nullable(String type) {
	return 	(this.complianceLevel < ClassFileConstants.JDK1_8)
			? "Null type mismatch: required \'@NonNull "+type+"\' but the provided value is specified as @Nullable\n"
			: "Null type mismatch (type annotations): required '@NonNull "+type+"' but this expression has type '@Nullable "+type+"'\n";
}
String nullTypeSafety() {
	return (this.complianceLevel < ClassFileConstants.JDK1_8)
			? "Null type safety: "
			: "Null type safety (type annotations): ";
}
String variableMayBeNull(String var) {
	return 	(this.complianceLevel < ClassFileConstants.JDK1_8)
			? "Potential null pointer access: The variable "+var+" may be null at this location\n"
			: "Potential null pointer access: this expression has a '@Nullable' type\n";
}
String redundant_check_nonnull(String expr, String type) {
	return this.complianceLevel < ClassFileConstants.JDK1_8
			? "Redundant null check: "+expr+" is specified as @NonNull\n"
			: "Redundant null check: comparing '"+type+"' against null\n";
}
String redundantCheck_method_cannot_return_null(String method, String type) {
	return this.complianceLevel < ClassFileConstants.JDK1_8
			? "Redundant null check: The method "+method+" cannot return null\n"
			: "Redundant null check: comparing '@NonNull "+type+"' against null\n";
}
String checkAlwaysFalse_method_cannot_return_null(String method, String type) {
	return this.complianceLevel < ClassFileConstants.JDK1_8
			? "Null comparison always yields false: The method "+method+" cannot return null\n"
			: "Redundant null check: comparing '@NonNull "+type+"' against null\n";
}
String redundant_check_canonlynull(String expr, String type) {
	return this.complianceLevel < ClassFileConstants.JDK1_8
			? "Redundant null check: "+expr+" can only be null at this location\n"
			: "Redundant null check: comparing '@NonNull "+type+"' against null\n";
}

String checkAlwaysFalse_nonnull(String expr, String type) {
	return (this.complianceLevel < ClassFileConstants.JDK1_8)
		? "Null comparison always yields false: "+expr+" is specified as @NonNull\n"
		: "Redundant null check: comparing '@NonNull "+type+"' against null\n";
}
String potNPE_nullable(String expr) {
	return (this.complianceLevel < ClassFileConstants.JDK1_8)
		? "Potential null pointer access: "+expr+" is specified as @Nullable\n"
		: "Potential null pointer access: this expression has a '@Nullable' type\n";
}
String potNPE_nullable_maybenull(String expr) {
	return (this.complianceLevel < ClassFileConstants.JDK1_8)
		? "Potential null pointer access: "+expr+" may be null at this location\n"
		: "Potential null pointer access: this expression has a '@Nullable' type\n";
}
String nonNullArrayOf(String string) {
	return (this.complianceLevel < ClassFileConstants.JDK1_8)
			? "@NonNull Object[]"
			: "Object @NonNull[]";
}


String targetTypeUseIfAvailable() {
	return this.complianceLevel >= ClassFileConstants.JDK1_8
				? "@Target(ElementType.TYPE_USE)\n"
				: "";
}

String cancenNonNullByDefault() {
	return this.complianceLevel < ClassFileConstants.JDK1_8
				? "    @NonNullByDefault(false)\n"
				: "    @NonNullByDefault({})\n";
}

/**
 * @deprecated
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	if (this.complianceLevel >= ClassFileConstants.JDK1_8)
		this.TEST_JAR_SUFFIX = "_1.8.jar";
	if (this.LIBS == null) {
		this.LIBS = getLibsWithNullAnnotations(this.complianceLevel);
	}
}

// a nullable argument is dereferenced without a check
public void test_nullable_paramter_001() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void foo(@Nullable Object o) {
				        System.out.print(o.toString());
				    }
				}
				"""},
	    "----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	System.out.print(o.toString());\n" +
		"	                 ^\n" +
		variableMayBeNull("o") +
		"----------\n");
}

// a null value is passed to a nullable argument
public void test_nullable_paramter_002() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void foo(@Nullable Object o) {
				        // nop
				    }
				    void bar() {
				        foo(null);
				    }
				}
				"""},
		null,
	    "");
}

// a non-null argument is checked for null
public void test_nonnull_parameter_001() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void foo(@NonNull Object o) {
				        if (o != null)
				              System.out.print(o.toString());
				    }
				}
				"""},
	    "----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null)\n" +
		"	    ^\n" +
		redundant_check_nonnull("The variable o", "@NonNull Object") +
		"----------\n");
}
// a non-null argument is dereferenced without a check
public void test_nonnull_parameter_002() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void foo(@NonNull Object o) {
				        System.out.print(o.toString());
				    }
				    public static void main(String... args) {
				        new X().foo("OK");
				    }
				}
				"""},
		getCompilerOptions(),
		"",
	    "OK");
}
// passing null to nonnull parameter - many fields in enclosing class
public void test_nonnull_parameter_003() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    int i00, i01, i02, i03, i04, i05, i06, i07, i08, i09;\
				    int i10, i11, i12, i13, i14, i15, i16, i17, i18, i19;\
				    int i20, i21, i22, i23, i24, i25, i26, i27, i28, i29;\
				    int i30, i31, i32, i33, i34, i35, i36, i37, i38, i39;\
				    int i40, i41, i42, i43, i44, i45, i46, i47, i48, i49;\
				    int i50, i51, i52, i53, i54, i55, i56, i57, i58, i59;\
				    int i60, i61, i62, i63, i64, i65, i66, i67, i68, i69;\
				    void foo(@NonNull Object o) {
				        System.out.print(o.toString());
				    }
				    void bar() {
				        foo(null);
				    }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 7)
				foo(null);
				    ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// passing potential null to nonnull parameter - target method is consumed from .class
public void test_nonnull_parameter_004() {
	runConformTestWithLibs(
			new String[] {
				"Lib.java",
					"""
						import org.eclipse.jdt.annotation.*;
						public class Lib {
						    void setObject(@NonNull Object o) { }
						}
						"""
			},
			null /*customOptions*/,
			"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			  """
				public class X {
				    void bar(Lib l, boolean b) {
				        Object o = null;
				        if (b) o = new Object();
				        l.setObject(o);
				    }
				}
				"""},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 5)
				l.setObject(o);
				            ^
			Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable
			----------
			""");
}
// passing unknown value to nonnull parameter  - target method is consumed from .class
public void test_nonnull_parameter_005() {
	runConformTestWithLibs(
			new String[] {
				"Lib.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class Lib {
					    void setObject(@NonNull Object o) { }
					}
					"""
			},
			null /*customOptions*/,
			"");
	runWarningTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			  """
				public class X {
				    void bar(Lib l, Object o) {
				        l.setObject(o);
				    }
				}
				"""},
		null /* options */,
		"----------\n" +
		"1. WARNING in X.java (at line 3)\n" +
		"	l.setObject(o);\n" +
		"	            ^\n" +
		nullTypeSafety() + "The expression of type 'Object' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}
// a ternary non-null expression is passed to a nonnull parameter
public void test_nonnull_parameter_006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    	void m1(@NonNull String a) {}
						void m2(@Nullable String b) {
							m1(b == null ? "" : b);
						}
				}
				"""},
		customOptions,
		""  /* compiler output */);
}
// nullable value passed to a non-null parameter in a super-call
public void test_nonnull_parameter_007() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"XSub.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class XSub extends XSuper {
				    	XSub(@Nullable String b) {
							super(b);
						}
				}
				""",
			"XSuper.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class XSuper {
				    	XSuper(@NonNull String b) {
						}
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. ERROR in XSub.java (at line 4)\n" +
		"	super(b);\n" +
		"	      ^\n" +
		mismatch_NonNull_Nullable("String") +
		"----------\n");
}
// a nullable value is passed to a non-null parameter in an allocation expression
public void test_nonnull_parameter_008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    	X(@NonNull String a) {}
						static X create(@Nullable String b) {
							return new X(b);
						}
				}
				"""},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return new X(b);\n" +
		"	             ^\n" +
		mismatch_NonNull_Nullable("String") +
		"----------\n"  /* compiler output */);
}
// a nullable value is passed to a non-null parameter in a qualified allocation expression
public void test_nonnull_parameter_009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    class Local {
				    	   Local(@NonNull String a) {}
				    }
					   Local create(@Nullable String b) {
					       return this.new Local(b);
				    }
				}
				"""},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	return this.new Local(b);\n" +
		"	                      ^\n" +
		mismatch_NonNull_Nullable("String") +
		"----------\n"  /* compiler output */);
}
// null is passed to a non-null parameter in a qualified allocation expression, across CUs
public void test_nonnull_parameter_010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"ContainingInner2.java",
			"""
				public class ContainingInner2 {
				    public ContainingInner2 (@org.eclipse.jdt.annotation.NonNull Object o) {
				    }
				    public class Inner {
				        public Inner (@org.eclipse.jdt.annotation.NonNull Object o) {
				        }
				    }
				}
				""",
			"X.java",
			"""
				public class X {
					 void create() {
				          ContainingInner2 container = new ContainingInner2(null);
					       ContainingInner2.Inner inner = container.new Inner(null);
				    }
				}
				"""},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 3)
				ContainingInner2 container = new ContainingInner2(null);
				                                                  ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in X.java (at line 4)
				ContainingInner2.Inner inner = container.new Inner(null);
				                                                   ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// null is passed to a non-null parameter in a qualified allocation expression, target class read from .class
public void test_nonnull_parameter_011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
			new String[] {
				"ContainingInner2.java",
				"""
					public class ContainingInner2 {
					    public ContainingInner2 (@org.eclipse.jdt.annotation.NonNull Object o) {
					    }
					    public class Inner {
					        public Inner (@org.eclipse.jdt.annotation.NonNull Object o) {
					        }
					    }
					}
					""",
			},
			null /*customOptions*/,
			"");
	runNegativeTestWithLibs(
		false, // flush directory
		new String[] {
			"X.java",
			"""
				public class X {
					 void create() {
				          ContainingInner2 container = new ContainingInner2(null);
					       ContainingInner2.Inner inner = container.new Inner(null);
				    }
				}
				"""},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 3)
				ContainingInner2 container = new ContainingInner2(null);
				                                                  ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in X.java (at line 4)
				ContainingInner2.Inner inner = container.new Inner(null);
				                                                   ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// null is passed to a non-null parameter in a qualified allocation expression, generic constructor, target class read from .class
// Note: in new type inference we infer the parameter of the Inner ctor to NullTypeBinding.
// This needs special treatment in ctor of ParameterizedGenericMethodBinding and in Statement.analyseOneArgument18
// as to propagate nonnull info, although the NullTypeBinding cannot transport this info.
public void test_nonnull_parameter_012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
			new String[] {
				"ContainingInner2.java",
				"""
					public class ContainingInner2 {
					    public ContainingInner2 (@org.eclipse.jdt.annotation.NonNull Object o) {
					    }
					    public class Inner {
					        public <T> Inner (@org.eclipse.jdt.annotation.NonNull T o) {
					        }
					    }
					}
					""",
			},
			null /*customOptions*/,
			"");
	runNegativeTestWithLibs(
		false, // flush directory
		new String[] {
			"X.java",
			"""
				public class X {
					 void create() {
				          ContainingInner2 container = new ContainingInner2(null);
					       ContainingInner2.Inner inner = container.new Inner(null);
				    }
				}
				"""},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 3)
				ContainingInner2 container = new ContainingInner2(null);
				                                                  ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in X.java (at line 4)
				ContainingInner2.Inner inner = container.new Inner(null);
				                                                   ^^^^
			Null type mismatch: required \'@NonNull Object' but the provided value is null
			----------
			""");
}
// a method of a local class has a non-null parameter, client passes null
public void test_nonnull_parameter_013() {
	runNegativeTestWithLibs(
		new String[] {
			"B.java",
			"""
				class B {
				    void bar () {
				        class Local {
				            void callMe(@org.eclipse.jdt.annotation.NonNull Object o){
				            }
				        }
				        Local l = new Local();
				        l.callMe(null);
				    }\s
				}
				"""
		},
		"""
			----------
			1. ERROR in B.java (at line 8)
				l.callMe(null);
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// non-null varargs (message send)
public void test_nonnull_parameter_015() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			((this.complianceLevel < ClassFileConstants.JDK1_8)
			 ? "    void foo(@NonNull Object ... o) {\n"
			 : "    void foo(Object @NonNull... o) {\n") +
			"        if (o != null)\n" +
			"              System.out.print(o.toString());\n" +
			"    }\n" +
			((this.complianceLevel < ClassFileConstants.JDK1_8)
			? "    void foo2(int i, @NonNull Object ... o) {\n"
			: "    void foo2(int i, Object @NonNull ... o) {\n"
			) +
			"        if (o.length > 0 && o[0] != null)\n" +
			"              System.out.print(o[0].toString());\n" +
			"    }\n" +
			"    void bar() {\n" +
			"        foo((Object)null);\n" +		// unchecked: single plain argument
			"        Object[] objs = null;\n" +
			"        foo(objs);\n" +				// error
			"        foo(this, null);\n" +			// unchecked: multiple plain arguments
			"        foo2(2, (Object)null);\n" +    // unchecked: single plain argument
			"        foo2(2, null, this);\n" +      // unchecked: multiple plain arguments
			"        foo2(2, null);\n" +  			// error
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null)\n" +
		"	    ^\n" +
		redundant_check_nonnull("The variable o", "Object @NonNull[]") +
		"----------\n" +
		"2. ERROR in X.java (at line 14)\n" +
		"	foo(objs);\n" +
		"	    ^^^^\n" +
		"Null type mismatch: required \'"+nonNullArrayOf("Object")+"\' but the provided value is null\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 18)\n" +
		"	foo2(2, null);\n" +
		"	^^^^^^^^^^^^^\n" +
		"Type null of the last argument to method foo2(int, Object...) doesn't exactly match the vararg parameter type. Cast to Object[] to confirm the non-varargs invocation, or pass individual arguments of type Object for a varargs invocation.\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 18)\n" +
		"	foo2(2, null);\n" +
		"	        ^^^^\n" +
		"Null type mismatch: required \'"+nonNullArrayOf("Object")+"\' but the provided value is null\n" +
		"----------\n");
}
// non-null varargs (allocation and explicit constructor calls)
public void test_nonnull_parameter_016() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			"public class X {\n" +
			((this.complianceLevel < ClassFileConstants.JDK1_8)
			 ? "    X(@NonNull Object ... o) {\n"
			 : "    X(Object @NonNull... o) {\n") +
			"        if (o != null)\n" +
			"              System.out.print(o.toString());\n" +
			"    }\n" +
			"    class Y extends X {\n" +
			((this.complianceLevel < ClassFileConstants.JDK1_8)
			 ? "    Y(int i, @NonNull Object ... o) {\n"
			 : "    Y(int i, Object @NonNull... o) {\n") +
			"        	super(i, (Object)null);\n" +
			"        }\n" +
			((this.complianceLevel < ClassFileConstants.JDK1_8)
			 ? "    Y(char c, @NonNull Object ... o) {\n"
			 : "    Y(char c, Object @NonNull... o) {\n") +
			"        	this(1, new Object(), null);\n" +
			"        }\n" +
			"    }\n" +
			"    void bar() {\n" +
			"        new X((Object[])null);\n" +
			"        new X(this, null);\n" +
			"        X x = new X(null, this);\n" +
			"        x.new Y(2, (Object)null);\n" +
			"        this.new Y(2, null, this);\n" +
			"        this.new Y(2, (Object[])null);\n" +
			"    }\n" +
			"}\n"
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	if (o != null)\n" +
		"	    ^\n" +
		redundant_check_nonnull("The variable o", "Object @NonNull[]") +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	new X((Object[])null);\n" +
		"	      ^^^^^^^^^^^^^^\n" +
		"Null type mismatch: required \'"+nonNullArrayOf("Object")+"\' but the provided value is null\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 21)\n" +
		"	this.new Y(2, (Object[])null);\n" +
		"	              ^^^^^^^^^^^^^^\n" +
		"Null type mismatch: required \'"+nonNullArrayOf("Object")+"\' but the provided value is null\n" +
		"----------\n");
}
// Bug 367203 - [compiler][null] detect assigning null to nonnull argument
public void test_nonnull_argument_001() {
	runNegativeTestWithLibs(
			new String[] {
				"ShowNPE2.java",
				"""
					import org.eclipse.jdt.annotation.NonNullByDefault;
					@NonNullByDefault
					public class ShowNPE2 {
					     public Object foo(Object o1, final boolean b) {
					         o1 = null;   // expect NPE error
					         System.out.println(o1.toString());  \s
					         return null;  // expect NPE error
					    }
					}"""
			},
			"""
				----------
				1. ERROR in ShowNPE2.java (at line 5)
					o1 = null;   // expect NPE error
					     ^^^^
				Null type mismatch: required \'@NonNull Object\' but the provided value is null
				----------
				2. ERROR in ShowNPE2.java (at line 7)
					return null;  // expect NPE error
					       ^^^^
				Null type mismatch: required \'@NonNull Object\' but the provided value is null
				----------
				""");
}
// Bug 367203 - [compiler][null] detect assigning null to nonnull argument
public void test_nonnull_argument_002() {
	runNegativeTestWithLibs(
			new String[] {
				"ShowNPE2.java",
				"""
					import org.eclipse.jdt.annotation.NonNullByDefault;
					@NonNullByDefault
					public class ShowNPE2 {
					    public Object foo(Object o1, final boolean b) {
					        bar(o1); // expecting no problem
					        return null;  // expect NPE error
					    }
					    void bar(Object o2) {}
					}"""
			},
			"""
				----------
				1. ERROR in ShowNPE2.java (at line 6)
					return null;  // expect NPE error
					       ^^^^
				Null type mismatch: required \'@NonNull Object\' but the provided value is null
				----------
				""");
}
// a method of a local class has a non-null parameter, client passes potential null (msg send)
public void test_nonnull_parameter_014() {
	runNegativeTestWithLibs(
		new String[] {
			"B.java",
			"""
				class B {
				    void bar () {
				        class Local {
				            void callMe(@org.eclipse.jdt.annotation.NonNull Object o){
				            }
				        }
				        Local l = new Local();
				        l.callMe(getNull());
				    }
				    @org.eclipse.jdt.annotation.Nullable Object getNull() { return null; }\
				}
				"""
		},
		"----------\n" +
		"1. ERROR in B.java (at line 8)\n" +
		"	l.callMe(getNull());\n" +
		"	         ^^^^^^^^^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n");
}
// assigning potential null to a nonnull local variable
public void test_nonnull_local_001() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void foo(boolean b, Object p) {
				        @NonNull Object o1 = b ? null : new Object();
				        @NonNull String o2 = "";
				        o2 = null;
				        @NonNull Object o3 = p;
				    }
				}
				"""},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	@NonNull Object o1 = b ? null : new Object();\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"	                     ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n"
		:
		"	                         ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n") +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	o2 = null;\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 7)\n" +
		"	@NonNull Object o3 = p;\n" +
		"	                     ^\n" +
		nullTypeSafety() + "The expression of type 'Object' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}

// assigning potential null to a nonnull local variable - separate decl and assignment
public void test_nonnull_local_002() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			  """
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void foo(boolean b, Object p) {
				        @NonNull Object o1;
				        o1 = b ? null : new Object();
				        @NonNull String o2;
				        o2 = "";
				        o2 = null;
				        @NonNull Object o3;
				        o3 = p;
				    }
				}
				"""},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o1 = b ? null : new Object();\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"	     ^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable\n"
		:
		"	         ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n") +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	o2 = null;\n" +
		"	     ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"3. WARNING in X.java (at line 10)\n" +
		"	o3 = p;\n" +
		"	     ^\n" +
		nullTypeSafety() + "The expression of type 'Object' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}

// a method tries to tighten the type specification, super declares parameter o as @Nullable
// other parameters: s is redefined from not constrained to @Nullable which is OK
//                   third is redefined from not constrained to @NonNull which is bad, too
public void test_parameter_specification_inheritance_001() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Lib {
				    void foo(String s, @Nullable Object o, Object third) { }
				}
				"""
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X extends Lib {
				    @Override
				    void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }
				                             ^^^^^^^^^^^^^^^
			Illegal redefinition of parameter o, inherited method from Lib declares this parameter as @Nullable
			----------
			2. ERROR in X.java (at line 4)
				void foo(@Nullable String s, @NonNull Object o, @NonNull Object third) { System.out.print(o.toString()); }
				                                                ^^^^^^^^^^^^^^^
			Illegal redefinition of parameter third, inherited method from Lib does not constrain this parameter
			----------
			""");
}
// a method body fails to redeclare the inherited null annotation, super declares parameter as @Nullable
public void test_parameter_specification_inheritance_002() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Lib {
				    void foo(@Nullable Object o) { }
				}
				"""
		},
		null,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"""
				public class X extends Lib {
				    @Override
				    void foo(Object o) {
				        System.out.print(o.toString());
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 3)
				void foo(Object o) {
				         ^^^^^^
			Missing nullable annotation: inherited method from Lib specifies this parameter as @Nullable
			----------
			""");
}
// a method relaxes the parameter null specification, super interface declares parameter o as @NonNull
// other (first) parameter just repeats the inherited @NonNull
public void test_parameter_specification_inheritance_003() {
	runConformTestWithLibs(
		new String[] {
			"IX.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public interface IX {
				    void foo(@NonNull String s, @NonNull Object o);
				}
				""",
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X implements IX {
				    public void foo(@NonNull String s, @Nullable Object o) { ; }
				    void bar() { foo("OK", null); }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// a method adds a @NonNull annotation, super interface has no null annotation
// changing other from unconstrained to @Nullable is OK
public void test_parameter_specification_inheritance_004() {
	runConformTestWithLibs(
		new String[] {
			"IX.java",
			"""
				public interface IX {
				    void foo(Object o, Object other);
				}
				"""
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X implements IX {
				    public void foo(@NonNull Object o, @Nullable Object other) { System.out.print(o.toString()); }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(@NonNull Object o, @Nullable Object other) { System.out.print(o.toString()); }
				                ^^^^^^^^^^^^^^^
			Illegal redefinition of parameter o, inherited method from IX does not constrain this parameter
			----------
			""");
}
// a method tries to relax the null contract, super declares @NonNull return
public void test_parameter_specification_inheritance_005() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Lib {
				    @NonNull Object getObject() { return new Object(); }
				}
				"""
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, //dont' flush
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X extends Lib {
				    @Override
				    @Nullable Object getObject() { return null; }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				@Nullable Object getObject() { return null; }
				^^^^^^^^^^^^^^^^
			The return type is incompatible with '@NonNull Object' returned from Lib.getObject() (mismatching null constraints)
			----------
			""");
}

// super has no constraint for return, sub method confirms the null contract as @Nullable
public void test_parameter_specification_inheritance_006() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				public class Lib {
				    Object getObject() { return null; }
				}
				"""
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X extends Lib {
				    @Override
				    @Nullable Object getObject() { return null; }
				}
				"""
		},
		null /*customOptions*/,
		"");
}
// a method body violates the inherited null specification, super declares @NonNull return, missing redeclaration
public void test_parameter_specification_inheritance_007() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Lib {
				    @NonNull Object getObject() { return new Object(); }
				}
				"""
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"""
				public class X extends Lib {
				    @Override
				    Object getObject() { return null; }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 3)
				Object getObject() { return null; }
				^^^^^^
			The return type is incompatible with '@NonNull Object' returned from Lib.getObject() (mismatching null constraints)
			----------
			""");
}
//a method body violates the @NonNull return specification (repeated from super)
public void test_parameter_specification_inheritance_007a() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Lib {
				    @NonNull Object getObject() { return new Object(); }
				}
				"""
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X extends Lib {
				    @Override
				    @NonNull Object getObject() { return null; }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				@NonNull Object getObject() { return null; }
				                                     ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// a client potentially violates the inherited null specification, super interface declares @NonNull parameter
public void test_parameter_specification_inheritance_008() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void printObject(@NonNull Object o) { System.out.print(o.toString()); }
				}
				"""
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"XSub.java",
			"""
				public class XSub extends X {
				    @Override
				    public void printObject(Object o) { super.printObject(o); }
				}
				""",
			"M.java",
			"""
				public class M{
				    void foo(X x, Object o) {
				        x.printObject(o);
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. WARNING in XSub.java (at line 3)\n" +
		"	public void printObject(Object o) { super.printObject(o); }\n" +
		"	                        ^^^^^^\n" +
		"Missing non-null annotation: inherited method from X specifies this parameter as @NonNull\n" +
		"----------\n" +
		"2. ERROR in XSub.java (at line 3)\n" +
		"	public void printObject(Object o) { super.printObject(o); }\n" +
		"	                                                      ^\n" +
		nullTypeSafety() + "The expression of type 'Object' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in M.java (at line 3)\n" +
		"	x.printObject(o);\n" +
		"	              ^\n" +
		nullTypeSafety() + "The expression of type 'Object' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}
// a static method has a more relaxed null contract than a like method in the super class, but no overriding.
public void test_parameter_specification_inheritance_009() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Lib {
				    @NonNull static Object getObject() { return new Object(); }
				}
				""",
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X extends Lib {
				    @Nullable static Object getObject() { return null; }
				}
				"""
		},
		null /*customOptions*/,
		"");
}
// class default is nonnull, method and its super both use the default
public void test_parameter_specification_inheritance_010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    protected String getString(String s) {
				        if (Character.isLowerCase(s.charAt(0)))
					        return getString(s);
					     return s;
				    }
				}
				""",
	"p1/Y.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y extends X {
				    @Override
				    protected String getString(String s) {
					     return super.getString(s);
				    }
				}
				""",
		},
		customOptions,
		"");
}
// class default is nonnull, method and its super both use the default, super-call passes null
public void test_parameter_specification_inheritance_011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    protected String getString(String s) {
				        if (Character.isLowerCase(s.charAt(0)))
					        return getString(s);
					     return s;
				    }
				}
				""",
	"p1/Y.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y extends X {
				    @Override
				    protected String getString(String s) {
					     return super.getString(null);
				    }
				}
				""",
		},
		customOptions,
		"""
			----------
			1. ERROR in p1\\Y.java (at line 7)
				return super.getString(null);
				                       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			""");
}
// methods from two super types have different null contracts.
// sub-class merges both using the weakest common contract
public void test_parameter_specification_inheritance_012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public class X {
				    public @Nullable String getString(String s1, @Nullable String s2, @NonNull String s3) {
					     return s1;
				    }
				}
				""",
	"p1/IY.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public interface IY {
				    @NonNull String getString(@NonNull String s1, @NonNull String s2, @Nullable String s3);
				}
				""",
	"p1/Y.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public class Y extends X implements IY {
				    @Override
				    public @NonNull String getString(@Nullable String s1, @Nullable String s2, @Nullable String s3) {
					     return "";
				    }
				}
				""",
		},
		customOptions,
		"");
}
// methods from two super types have different null contracts.
// sub-class overrides this method in non-conforming ways
public void test_parameter_specification_inheritance_013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public class X {
				    public @Nullable String getString(String s1, @Nullable String s2, @NonNull String s3) {
					     return s1;
				    }
				}
				""",
	"p1/IY.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public interface IY {
				    @NonNull String getString(@NonNull String s1, @NonNull String s2, @Nullable String s3);
				}
				""",
	"p1/Y.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public class Y extends X implements IY {
				    @Override
				    public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {
					     return "";
				    }
				}
				""",
		},
		customOptions,
		"""
			----------
			1. ERROR in p1\\Y.java (at line 5)
				public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {
				       ^^^^^^^^^^^^^^^^
			The return type is incompatible with '@NonNull String' returned from IY.getString(String, String, String) (mismatching null constraints)
			----------
			2. ERROR in p1\\Y.java (at line 5)
				public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {
				                                             ^^^^^^^^^^^^^^^
			Illegal redefinition of parameter s2, inherited method from X declares this parameter as @Nullable
			----------
			3. ERROR in p1\\Y.java (at line 5)
				public @Nullable String getString(String s1, @NonNull String s2, @NonNull String s3) {
				                                                                 ^^^^^^^^^^^^^^^
			Illegal redefinition of parameter s3, inherited method from IY declares this parameter as @Nullable
			----------
			""");
}
// methods from two super types have different null contracts.
// sub-class does not override, but should to bridge the incompatibility
public void test_parameter_specification_inheritance_014() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
	"p1/IY.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public interface IY {
				    public @NonNull String getString1(String s);
				    public @NonNull String getString2(String s);
				    public String getString3(@Nullable String s);
				    public @NonNull String getString4(@Nullable String s);
				    public @NonNull String getString5(@Nullable String s);
				    public @Nullable String getString6(@NonNull String s);
				}
				""",
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public class X {
				    public @Nullable String getString1(String s) {
					     return s;
				    }
				    public String getString2(String s) {
					     return s;
				    }
				    public String getString3(String s) {
					     return "";
				    }
				    public @NonNull String getString4(@Nullable String s) {
					     return "";
				    }
				    public @NonNull String getString5(@NonNull String s) {
					     return s;
				    }
				    public @NonNull String getString6(@Nullable String s) {
					     return "";
				    }
				}
				""",
	"p1/Y.java",
			"""
				package p1;
				public class Y extends X implements IY {
				}
				""",
		},
		customOptions,
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"""
			----------
			1. ERROR in p1\\Y.java (at line 2)
				public class Y extends X implements IY {
				                       ^
			The method getString1(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints
			----------
			2. ERROR in p1\\Y.java (at line 2)
				public class Y extends X implements IY {
				                       ^
			The method getString2(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints
			----------
			3. ERROR in p1\\Y.java (at line 2)
				public class Y extends X implements IY {
				                       ^
			The method getString5(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints
			----------
			4. ERROR in p1\\Y.java (at line 2)
				public class Y extends X implements IY {
				                       ^
			The method getString3(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints
			----------
			"""
		: """
			----------
			1. ERROR in p1\\Y.java (at line 2)
				public class Y extends X implements IY {
				                       ^
			The method @Nullable String getString1(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints
			----------
			2. ERROR in p1\\Y.java (at line 2)
				public class Y extends X implements IY {
				                       ^
			The method String getString2(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints
			----------
			3. ERROR in p1\\Y.java (at line 2)
				public class Y extends X implements IY {
				                       ^
			The method getString5(@NonNull String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints
			----------
			4. ERROR in p1\\Y.java (at line 2)
				public class Y extends X implements IY {
				                       ^
			The method getString3(String) from X cannot implement the corresponding method from IY due to incompatible nullness constraints
			----------
			"""));
}
// a method relaxes the parameter null specification from @NonNull to un-annotated
// see https://bugs.eclipse.org/381443
public void test_parameter_specification_inheritance_015() {
	runWarningTestWithLibs(
		true, // flush
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void foo(@NonNull String s) { System.out.println(s); }
				}
				""",
			"XSub.java",
			"""
				public class XSub extends X {
					 @Override
				    public void foo(String s) { if (s != null) super.foo(s); }
				    void bar() { foo(null); }
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in XSub.java (at line 3)
				public void foo(String s) { if (s != null) super.foo(s); }
				                ^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			""");
}

// a method relaxes the parameter null specification from @NonNull to un-annotated
// see https://bugs.eclipse.org/381443
// issue configured as error
public void test_parameter_specification_inheritance_016() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void foo(@NonNull String s) { System.out.println(s); }
				}
				""",
			"XSub.java",
			"""
				public class XSub extends X {
				    @Override
				    public void foo(String s) { if (s != null) super.foo(s); }
				    void bar() { foo(null); }
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in XSub.java (at line 3)
				public void foo(String s) { if (s != null) super.foo(s); }
				                ^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			""");
}

// a class inherits two methods with different spec: one non-null param & one unannotated param
// widening reported as warning by default
// see https://bugs.eclipse.org/381443
public void test_parameter_specification_inheritance_017() {
	runWarningTestWithLibs(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
				    public void foo(String s) { System.out.println(s); }
				}
				""",
			"IX.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public interface IX {
				    void foo(@NonNull String s);
				}
				""",
			"XSub.java",
			"""
				public class XSub extends X implements IX {
				    void bar() { foo(null); }
				    static void zork(XSub sub) {
				        sub.foo(null);
				    }
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in XSub.java (at line 1)
				public class XSub extends X implements IX {
				                          ^
			Parameter 1 of method foo(String) lacks a @NonNull annotation as specified in type IX
			----------
			""");
}

// a class inherits two methods with different spec: one non-null param & one unannotated param
// opt to accept this widening
// see https://bugs.eclipse.org/381443
public void test_parameter_specification_inheritance_018() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_NONNULL_PARAMETER_ANNOTATION_DROPPED, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
				    public void foo(String s) { System.out.println(s); }
				}
				""",
			"IX.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public interface IX {
				    void foo(@NonNull String s);
				}
				""",
			"XSub.java",
			"""
				public class XSub extends X implements IX {
				    void bar() { foo(null); }
				    static void zork(XSub sub) {
				        sub.foo(null);
				    }
				}
				"""
		},
		options,
		"");
}

// a nullable return value is dereferenced without a check
public void test_nullable_return_001() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object getObject() { return null; }
				    void foo() {
				        Object o = getObject();
				        System.out.print(o.toString());
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				System.out.print(o.toString());
				                 ^
			Potential null pointer access: The variable o may be null at this location
			----------
			""");
}
// a nullable return value is dereferenced without a check, method is read from .class file
public void test_nullable_return_002() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Lib {
				    @Nullable Object getObject() { return null; }
				}
				"""
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(Lib l) {
				        Object o = l.getObject();
				        System.out.print(o.toString());
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				System.out.print(o.toString());
				                 ^
			Potential null pointer access: The variable o may be null at this location
			----------
			""");
}
// a non-null return value is checked for null, method is read from .class file
public void test_nonnull_return_001() {
	runConformTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Lib {
				    @NonNull Object getObject() { return new Object(); }
				}
				"""
		},
		null /*customOptions*/,
		"");
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
			"X.java",
			"""
				public class X {
				    void foo(Lib l) {
				        Object o = l.getObject();
				        if (o != null)
				            System.out.print(o.toString());
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (o != null)
				    ^
			Redundant null check: The variable o cannot be null at this location
			----------
			""");
}
// a non-null method returns null
public void test_nonnull_return_003() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object getObject(boolean b) {
				        if (b)
				            return null;
				        return new Object();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				return null;
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// a non-null method potentially returns null
public void test_nonnull_return_004() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object getObject(@Nullable Object o) {
				        return o;
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return o;\n" +
		"	       ^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n");
}
// a non-null method returns its non-null argument
public void test_nonnull_return_005() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object getObject(@NonNull Object o) {
				        return o;
				    }
				}
				"""
		},
		null, // options
		"");
}
//a non-null method has insufficient nullness info for its return value
public void test_nonnull_return_006() {
	runWarningTestWithLibs(
		true, // flush
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object getObject(Object o) {
				        return o;
				    }
				}
				"""
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	return o;\n" +
		"	       ^\n" +
		nullTypeSafety() + "The expression of type 'Object' needs unchecked conversion to conform to \'@NonNull Object\'\n" +
		"----------\n");
}
// a result from a nullable method is directly dereferenced
public void test_nonnull_return_007() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object getObject() {
				        return null;
				    }
				    void test() {
				        getObject().toString();
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				getObject().toString();
				^^^^^^^^^^^
			Potential null pointer access: The method getObject() may return null
			----------
			""");
}
// a result from a nonnull method is directly checked for null: redundant
public void test_nonnull_return_008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object getObject() {
				        return new Object();
				    }
				    void test() {
				        if (getObject() == null)
						     throw new RuntimeException();
				    }
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (getObject() == null)\n" +
		"	    ^^^^^^^^^^^\n" +
		checkAlwaysFalse_method_cannot_return_null("getObject()", "Object") +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	throw new RuntimeException();\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n");
}
// a result from a nonnull method is directly checked for null (from local): redundant
public void test_nonnull_return_009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object getObject() {
				        return new Object();
				    }
				    void test() {
				        Object left = null;
				        if (left != getObject())
						     throw new RuntimeException();
				    }
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	if (left != getObject())\n" +
		"	    ^^^^\n" +
		"Redundant null check: The variable left can only be null at this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	if (left != getObject())\n" +
		"	            ^^^^^^^^^^^\n" +
		redundantCheck_method_cannot_return_null("getObject()", "Object") +
		"----------\n");
}
// a result from a nonnull method is directly checked for null (from local): not redundant due to loop
public void test_nonnull_return_009a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object getObject() {
				        return new Object();
				    }
				    void test() {
				        Object left = null;
				        for (int i=0; i<3; i++) {
				            if (left != getObject())
					    	     throw new RuntimeException();
				            left = new Object();
				        }
				    }
				}
				"""
		},
		customOptions,
		"");
}
// a result from a nonnull method is directly checked for null (from local): redundant despite loop
// disabled because only one of two desirable errors is raised
// need to integrate @NonNull expressions (MessageSend and more) into deferred analysis by FlowContext
public void _test_nonnull_return_009b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object getObject() {
				        return new Object();
				    }
				    void test() {
				        Object left = null;
				        for (int i=0; i<3; i++) {
				            if (left != getObject())
					    	     throw new RuntimeException();
				            // left remains null
				        }
				    }
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	if (left != getObject())\n" +
		"	    ^^^^\n" +
		redundant_check_canonlynull("The variable left", "Object") +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	if (left != getObject())\n" +
		"	            ^^^^^^^^^^^\n" +
		"Redundant null check: The method getObject() cannot return null\n" +
		"----------\n");
}
// a result from a nullable method is assigned and checked for null (from local): not redundant
// see also Bug 336428 - [compiler][null] bogus warning "redundant null check" in condition of do {} while() loop
public void test_nonnull_return_010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable X getX() {
				        return new X();
				    }
				    void test() {
				        X left = this;
				        do {
				            if (left == null)\s
					   	         throw new RuntimeException();
				        } while ((left = left.getX()) != null);
				    }
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 9)
				if (left == null)\s
				    ^^^^
			Null comparison always yields false: The variable left cannot be null at this location
			----------
			""");
}
// a non-null method returns a checked-for null value, but that branch is dead code
public void test_nonnull_return_011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    Object getObject(Object dubious) {
				        if (dubious == null)
				            return dubious;
				        return new Object();
				    }
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (dubious == null)\n" +
		"	    ^^^^^^^\n" +
		((this.complianceLevel < ClassFileConstants.JDK1_8)
			? "Null comparison always yields false: The variable dubious is specified as @NonNull\n"
			: "Redundant null check: comparing '@NonNull Object' against null\n" ) +
		"----------\n" +
		"2. WARNING in X.java (at line 6)\n" +
		"	return dubious;\n" +
		"	^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n");
}
// a non-null method returns a definite null from a conditional expression
// requires the fix for Bug 354554 - [null] conditional with redundant condition yields weak error message
// TODO(SH): ENABLE!
public void _test_nonnull_return_012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    Object getObject(Object dubious) {
				        return dubious == null ? dubious : null;
				    }
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 5)
				return dubious == null ? dubious : null;
				       ^^^^^^^
			Null comparison always yields false: The variable dubious cannot be null at this location
			----------
			2. ERROR in X.java (at line 5)
				return dubious == null ? dubious : null;
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// don't apply any default annotations to return void
public void test_nonnull_return_013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    void getObject() {}
				}
				""",
			"Y.java",
			"""
				public class Y extends X {
				    @Override
				    void getObject() {}
				}
				"""
		},
		customOptions,
		"");
}
// bug 365835: [compiler][null] inconsistent error reporting.
public void test_nonnull_return_014() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				
				public class X {
					@NonNull
					public Object foo(Object x, int y) {
						@NonNull Object local;
						while (true) {
							if (y == 4) {
								local = x;  // error
								return x;   // only a warning.
							}
							x = null;
						}
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				local = x;  // error
				        ^
			Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable
			----------
			2. ERROR in X.java (at line 10)
				return x;   // only a warning.
				       ^
			Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable
			----------
			""");
}
// suppress an error regarding null-spec violation
public void test_suppress_001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_SUPPRESS_OPTIONAL_ERRORS, JavaCore.ENABLED);
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
					    @SuppressWarnings("null")
					    @NonNull Object getObject(@Nullable Object o) {
					        return o;
					    }
					}
					"""
			},
			customOptions,
			"");
}
// mixed use of fully qualified name / explicit import
public void test_annotation_import_001() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runConformTestWithLibs(
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
			"Lib.java",
			"""
				public class Lib {
				    @org.foo.NonNull Object getObject() { return new Object(); }
				}
				""",
			"X.java",
			"""
				import org.foo.NonNull;
				public class X {
				    @NonNull Object getObject(@NonNull Lib l) {
				        return l.getObject();
				    }
				}
				"""
		},
		customOptions,
		"");
}

// use of explicit imports throughout
public void test_annotation_import_002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runConformTest(
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
			"Lib.java",
			"""
				import org.foo.NonNull;
				public class Lib {
				    @NonNull Object getObject() { return new Object(); }
				}
				""",
			"X.java",
			"""
				import org.foo.NonNull;
				public class X {
				    @NonNull Object getObject(@org.foo.Nullable String dummy, @NonNull Lib l) {
				        Object o = l.getObject();\
				        return o;
				    }
				}
				"""
		},
		customOptions,
		"");
}
// explicit import of existing annotation types
// using a Lib without null specifications
public void test_annotation_import_005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.MayBeNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.MustNotBeNull");
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"org/foo/MayBeNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			targetTypeUseIfAvailable() +
			"public @interface MayBeNull {}\n",

			"org/foo/MustNotBeNull.java",
			"package org.foo;\n" +
			"import java.lang.annotation.*;\n" +
			"@Retention(RetentionPolicy.CLASS)\n" +
			targetTypeUseIfAvailable() +
			"public @interface MustNotBeNull {}\n",

			"Lib.java",
			"""
				public class Lib {
				    public Object getObject() { return new Object(); }
				}
				""",
			"X.java",
			"""
				import org.foo.*;
				public class X {
				    @MustNotBeNull Object getObject(@MustNotBeNull Lib l) {
				        return l.getObject();
				    }
				}
				""",

		},
		null /*no libs*/,
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	return l.getObject();\n" +
		"	       ^^^^^^^^^^^^^\n" +
		nullTypeSafety() + "The expression of type 'Object' needs unchecked conversion to conform to \'@MustNotBeNull Object\'\n" +
		"----------\n",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// a non-null method returns a value obtained from an unannotated method, missing annotation types
public void test_annotation_import_006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.MayBeNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.MustNotBeNull");
	runNegativeTest(
		true/*shouldFlushOutputDirectory*/,
		new String[] {
			"Lib.java",
			"""
				public class Lib {
				    Object getObject() { return new Object(); }
				}
				""",
			"X.java",
			"""
				public class X {
				    @MustNotBeNull Object getObject(@MustNotBeNull Lib l) {
				        return l.getObject();
				    }
				}
				"""
		},
		null /* no libs */,
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 2)
				@MustNotBeNull Object getObject(@MustNotBeNull Lib l) {
				 ^^^^^^^^^^^^^
			MustNotBeNull cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 2)
				@MustNotBeNull Object getObject(@MustNotBeNull Lib l) {
				                                 ^^^^^^^^^^^^^
			MustNotBeNull cannot be resolved to a type
			----------
			""",
		JavacTestOptions.DEFAULT);
}

// a null annotation is illegally used on a class:
public void test_illegal_annotation_001() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNull public class X {
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 2)\n" +
		"	@NonNull public class X {\n" +
		"	^^^^^^^^\n" +
		((this.complianceLevel < ClassFileConstants.JDK1_8)
		? "The annotation @NonNull is disallowed for this location\n"
		: "The nullness annotation 'NonNull' is not applicable at this location\n") +
		"----------\n");
}
// this test has been removed:
// setting default to nullable, default applies to a parameter
// public void test_default_nullness_001()

// a null annotation is illegally defined by its simple name
// disabled because specific error is not currently raised
public void _test_illegal_annotation_002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "NichtNull");
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}\n"
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X {
				^
			Cannot use the unqualified name \'NichtNull\' as an annotation name for null specification
			----------
			""");
}

// a null annotation is illegally used on a void method:
public void test_illegal_annotation_003() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNull void foo() {}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	@NonNull void foo() {}\n" +
		"	^^^^^^^^\n" +
		((this.complianceLevel < ClassFileConstants.JDK1_8)
			? "The nullness annotation @NonNull is not applicable for the primitive type void\n"
			: "Type annotation is illegal for a method that returns void\n") +
		"----------\n",
		this.LIBS,
		false/*shouldFlush*/);
}

// a null annotation is illegally used on an int method:
public void test_illegal_annotation_003b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNull int foo() { return 1; }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				@NonNull int foo() { return 1; }
				^^^^^^^^
			The nullness annotation @NonNull is not applicable for the primitive type int
			----------
			""");
}

// a null annotation is illegally used on a primitive type parameter
public void test_illegal_annotation_004() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					void foo(@Nullable int i) {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				void foo(@Nullable int i) {}
				         ^^^^^^^^^
			The nullness annotation @Nullable is not applicable for the primitive type int
			----------
			""");
}

// a null annotation is illegally used on a primitive type local var
public void test_illegal_annotation_005() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					int foo() {
				       @Nullable int i = 3;
				       return i;
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				@Nullable int i = 3;
				^^^^^^^^^
			The nullness annotation @Nullable is not applicable for the primitive type int
			----------
			""");
}

// a configured annotation type does not exist
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=186342#c133
public void test_illegal_annotation_006() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "nullAnn.Nullable");
	runNegativeTest(
		new String[] {
			"p/Test.java",
			"""
				package p;
				import nullAnn.*;  // 1\s
				
				public class Test {\s
				
				        void foo(@nullAnn.Nullable  Object o) {   // 2
				            o.toString();          \s
				        }
				}"""
		},
		"""
			----------
			1. ERROR in p\\Test.java (at line 2)
				import nullAnn.*;  // 1\s
				       ^^^^^^^
			The import nullAnn cannot be resolved
			----------
			2. ERROR in p\\Test.java (at line 6)
				void foo(@nullAnn.Nullable  Object o) {   // 2
				          ^^^^^^^
			nullAnn cannot be resolved to a type
			----------
			""",
		this.LIBS,
		true, // flush
		customOptions);
}

// a configured annotation type does not exist
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=186342#c186
public void test_illegal_annotation_007() {
	Map customOptions = getCompilerOptions();
	runNegativeTest(
		new String[] {
			"p/Test.java",
			"""
				package p;
				import org.eclipse.jdt.annotation.*;
				interface TestInt{
					@NonNull Object foo();
				}
				
				public class Test {\s
					void bar() {\
						new TestInt() {
				        	@org public Object foo() {
				        	}
						};
					}
				}"""
		},
		"""
			----------
			1. ERROR in p\\Test.java (at line 9)
				@org public Object foo() {
				 ^^^
			org cannot be resolved to a type
			----------
			2. ERROR in p\\Test.java (at line 9)
				@org public Object foo() {
				            ^^^^^^
			The return type is incompatible with '@NonNull Object' returned from TestInt.foo() (mismatching null constraints)
			----------
			""",
		this.LIBS,
		true, // flush
		customOptions);
}

// a null annotation is illegally used on a constructor:
public void test_illegal_annotation_008() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNull X() {}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 3)\n" +
		"	@NonNull X() {}\n" +
		"	^^^^^^^^\n" +
		((this.complianceLevel < ClassFileConstants.JDK1_8)
		 ? "The annotation @NonNull is disallowed for this location\n"
		 : "The nullness annotation 'NonNull' is not applicable at this location\n" ) +
		"----------\n");
}

public void test_default_nullness_002() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
// This option currently does nothing:
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    Object getObject(@Nullable Object o) {
				        return new Object();
				    }
				}
				""",
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y extends X {
				    @Override
				    @Nullable Object getObject(Object o) {
				        return o;
				    }
				}
				""",
		},
		customOptions,
		"""
			----------
			1. ERROR in Y.java (at line 5)
				@Nullable Object getObject(Object o) {
				^^^^^^^^^^^^^^^^
			The return type is incompatible with '@NonNull Object' returned from X.getObject(Object) (mismatching null constraints)
			----------
			2. ERROR in Y.java (at line 5)
				@Nullable Object getObject(Object o) {
				                           ^^^^^^
			Illegal redefinition of parameter o, inherited method from X declares this parameter as @Nullable
			----------
			""");
}

public void test_default_nullness_002_custom() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runner.customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "org.foo.NonNullByDefault");
	runner.testFiles =
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			CUSTOM_NNBD_NAME,
			CUSTOM_NNBD_CONTENT,
			"X.java",
			"""
				import org.foo.*;
				@NonNullByDefault
				public class X {
				    Object getObject(@Nullable Object o) {
				        return new Object();
				    }
				}
				""",
			"Y.java",
			"""
				import org.foo.*;
				@NonNullByDefault
				public class Y extends X {
				    @Override
				    @Nullable Object getObject(Object o) {
				        return o;
				    }
				}
				""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in Y.java (at line 5)
				@Nullable Object getObject(Object o) {
				^^^^^^^^^^^^^^^^
			The return type is incompatible with '@NonNull Object' returned from X.getObject(Object) (mismatching null constraints)
			----------
			2. ERROR in Y.java (at line 5)
				@Nullable Object getObject(Object o) {
				                           ^^^^^^
			Illegal redefinition of parameter o, inherited method from X declares this parameter as @Nullable
			----------
			""";
	runner.runNegativeTest();
}

// package default is non-null
public void test_default_nullness_003() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    protected Object getObject(@Nullable Object o) {
				        return new Object();
				    }
				}
				""",
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p2;\n",
	"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				public class Y extends p1.X {
				    @Override
				    protected @Nullable Object getObject(@Nullable Object o) {
				        bar(o);
				        return o;
				    }
					 void bar(Object o2) { }
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p2\\Y.java (at line 5)\n" +
		"	protected @Nullable Object getObject(@Nullable Object o) {\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with '@NonNull Object' returned from X.getObject(Object) (mismatching null constraints)\n" +
		"----------\n" +
		"2. ERROR in p2\\Y.java (at line 6)\n" +
		"	bar(o);\n" +
		"	    ^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n");
}
// package level default is consumed from package-info.class, similarly for type level default
public void test_default_nullness_003a() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    protected Object getObject(@Nullable Object o) {
				        return new Object();
				    }
					 protected void bar(Object o2) { }
				}
				""",
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p2;\n",
			},
			customOptions,
			"");
	// check if default is visible from package-info.class.
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
	"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				public class Y extends p1.X {
				    @Override
				    protected @Nullable Object getObject(@Nullable Object o) {
				        bar(o);
				        accept(o);
				        return o;
				    }
				    void accept(Object a) {}
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p2\\Y.java (at line 5)\n" +
		"	protected @Nullable Object getObject(@Nullable Object o) {\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with '@NonNull Object' returned from X.getObject(Object) (mismatching null constraints)\n" +
		"----------\n" +
		"2. ERROR in p2\\Y.java (at line 6)\n" +
		"	bar(o);\n" +
		"	    ^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n" +
		"3. ERROR in p2\\Y.java (at line 7)\n" +
		"	accept(o);\n" +
		"	       ^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n");
}
// same as test_default_nullness_003a, but default-induced annotations are combined with explicit ones (not null related)
public void test_default_nullness_003b() {
	Map customOptions = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
	"p1/Annot.java",
			"""
				package p1;
				import static java.lang.annotation.ElementType.*;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({METHOD,PARAMETER})
				public @interface Annot {}
				""",
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    protected @Annot Object getObject(@Annot @Nullable Object o) {
				        return new Object();
				    }
					 protected @Annot void bar(@Annot Object o2) { }
				}
				""",
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p2;\n",
			},
			customOptions,
			"");
	// check if default is visible from package-info.class.
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
	"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				public class Y extends p1.X {
				    @Override
				    protected @Nullable Object getObject(@Nullable Object o) {
				        bar(o);
				        accept(o);
				        return o;
				    }
				    void accept(@p1.Annot Object a) {}
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. ERROR in p2\\Y.java (at line 5)\n" +
		"	protected @Nullable Object getObject(@Nullable Object o) {\n" +
		"	          ^^^^^^^^^^^^^^^^\n" +
		"The return type is incompatible with '@NonNull Object' returned from X.getObject(Object) (mismatching null constraints)\n" +
		"----------\n" +
		"2. ERROR in p2\\Y.java (at line 6)\n" +
		"	bar(o);\n" +
		"	    ^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n" +
		"3. ERROR in p2\\Y.java (at line 7)\n" +
		"	accept(o);\n" +
		"	       ^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n");
}
// package level default is consumed from package-info.class, similarly for type level default - fine tuned default
public void test_default_nullness_003c() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // uses version 2.0 of @NonNullByDefault
	Map customOptions = getCompilerOptions();
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    protected Object getObject(@Nullable Object o) {
				        return new Object();
				    }
					 protected void bar(Object o2) { }
				}
				""",
	"p2/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault({org.eclipse.jdt.annotation.DefaultLocation.PARAMETER})\n" +
			"package p2;\n",
			},
			customOptions,
			"");
	// check if default is visible from package-info.class.
	runNegativeTestWithLibs(
		false, // don't flush
		new String[] {
	"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				public class Y extends p1.X {
				    @Override
				    protected @Nullable Object getObject(@Nullable Object o) {
				        bar(o);
				        @NonNull Object nno = accept(o); // 2xERR
				        return o;
				    }
				    Object accept(Object a) { return a; }
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in p2\\Y.java (at line 5)
				protected @Nullable Object getObject(@Nullable Object o) {
				          ^^^^^^^^^^^^^^^^
			The return type is incompatible with '@NonNull Object' returned from X.getObject(Object) (mismatching null constraints)
			----------
			2. ERROR in p2\\Y.java (at line 6)
				bar(o);
				    ^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable Object\'
			----------
			3. WARNING in p2\\Y.java (at line 7)
				@NonNull Object nno = accept(o); // 2xERR
				                      ^^^^^^^^^
			Null type safety (type annotations): The expression of type \'Object\' needs unchecked conversion to conform to \'@NonNull Object\'
			----------
			4. ERROR in p2\\Y.java (at line 7)
				@NonNull Object nno = accept(o); // 2xERR
				                             ^
			Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable Object\'
			----------
			""");
}
// don't apply type-level default to non-reference type
public void test_default_nullness_004() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    protected Object getObject(boolean o) {
				        return new Object();
				    }
				}
				""",
	"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				public class Y extends p1.X {
				    @Override
				    protected @NonNull Object getObject(boolean o) {
				        return o ? this : new Object();
				    }
				}
				"""
		},
		customOptions,
		"");
}
// package default is non-null
// see also Bug 354536 - compiling package-info.java still depends on the order of compilation units
public void test_default_nullness_005() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				public class X {
				    class Inner {\
				        protected Object getObject(String s) {
				            return null;
				        }
				    }
				}
				""",
	"p1/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p1;\n",
	CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
	CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT
		},
		customOptions,
		"""
			----------
			1. ERROR in p1\\X.java (at line 4)
				return null;
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// package default is non-null, package-info.java read before the annotation type
// compile order: beginToCompile(X.Inner) triggers reading of package-info.java before the annotation type was read
public void test_default_nullness_006() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runNegativeTestWithLibs(
		new String[] {
	"p1/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p1;\n",
	"p1/X.java",
			"""
				package p1;
				public class X {
				    class Inner {\
				        protected Object getObject(String s) {
				            return null;
				        }
				    }
				}
				""",
	CUSTOM_NONNULL_NAME,
			CUSTOM_NONNULL_CONTENT,
	CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT
		},
		customOptions,
		"""
			----------
			1. ERROR in p1\\X.java (at line 4)
				return null;
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// global default nonnull, but return may be null
// DISABLED due to dysfunctional global default after Bug 366063 - Compiler should not add synthetic @NonNull annotations
public void _test_default_nullness_007() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object dangerous() {
				        return null;
				    }
				    Object broken() {
				        return dangerous();
				    }
				}
				""",

		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 7)
				return dangerous();
				       ^^^^^^^^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable
			----------
			""");
}

// cancel type level default to comply with super specification
public void test_default_nullness_008() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				public class X {
				    protected Object getObject(Object o) {
				        return new Object();
				    }
				}
				""",
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y extends p1.X {\n" +
			"    @Override\n" +
			cancenNonNullByDefault() +
			"    protected Object getObject(Object o) {\n" +
			"        if (o.toString().length() == 0)\n" + // dereference without a warning
			"	        return null;\n" + // return null without a warning
			"        return o.toString();\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"");
}

// cancel outer type level default to comply with super specification
public void test_default_nullness_009() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
	"p1/X.java",
			"""
				package p1;
				public class X {
				    protected Object getObject(Object o) {
				        return new Object();
				    }
				}
				""",
	"p2/Y.java",
			"package p2;\n" +
			"import org.eclipse.jdt.annotation.*;\n" +
			"@NonNullByDefault\n" +
			"public class Y { \n" +
			cancenNonNullByDefault() +
			"    static class Z extends p1.X {\n" +
			"        @Override\n" +
			"        protected Object getObject(Object o) {\n" +
			"            if (o.toString().length() == 0) {\n" +
			"                o = null;\n" + // assign null without a warning
			"                bar(o); // error: arg is declared @NonNull\n" +
			"	             return null;\n" +
			"            }\n" +
			"            return o.toString();\n" +
			"        }\n" +
			"        String bar(@NonNull Object o) {\n" +
			"            return getObject(o).toString();" +
			"        }\n" +
			"    }\n" +
			"}\n"
		},
		customOptions,
		"""
			----------
			1. ERROR in p2\\Y.java (at line 11)
				bar(o); // error: arg is declared @NonNull
				    ^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// non-null declarations are redundant within a default scope.
public void test_default_nullness_010() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runWarningTestWithLibs(
		true, // flush
		new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y {
				    protected @NonNull Object getObject(@NonNull Object o) {
				        return o;
				    }
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. WARNING in p2\\Y.java (at line 5)\n" +
		"	protected @NonNull Object getObject(@NonNull Object o) {\n" +
		"	          ^^^^^^^^^^^^^^^\n" +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"2. WARNING in p2\\Y.java (at line 5)\n" +
		"	protected @NonNull Object getObject(@NonNull Object o) {\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
	  ? "	                                    ^^^^^^^^^^^^^^^^^\n"
	  : "	                                    ^^^^^^^^^^^^^^^\n"
		) +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n");
}
// package-info declares nonnull-by-default
// special compile order due to import of type from that package
// cf. https://bugs.eclipse.org/bugs/show_bug.cgi?id=186342#add_comment
public void test_default_nullness_011() {
	runNegativeTestWithLibs(
		new String[] {
			"Main.java",
			"""
				import p1.C;
				public class Main {
				    void test(@org.eclipse.jdt.annotation.NonNull Object o) {
				        o = null;
				        new C(null);
				    }
				}
				""",
			"p1/C.java",
			"""
				package p1;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class C {
				    public C (Object o) {}
				}
				""",
			"p1/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p1;\n"
		},
		"""
			----------
			1. ERROR in Main.java (at line 4)
				o = null;
				    ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in Main.java (at line 5)
				new C(null);
				      ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			----------
			1. WARNING in p1\\C.java (at line 2)
				@org.eclipse.jdt.annotation.NonNullByDefault
				^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing package p1
			----------
			""");
}
// Bug 365836 - [compiler][null] Incomplete propagation of null defaults.
public void test_default_nullness_012() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				
				public class X {
				    @NonNullByDefault\s
				    public void foo(@Nullable String [] args) {
				        class local {
				            void zoo(Object o) {
				            }
				        };
				        new local().zoo(null); // defaults applying from foo
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				new local().zoo(null); // defaults applying from foo
				                ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// Bug 365836 - [compiler][null] Incomplete propagation of null defaults.
public void test_default_nullness_013() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				
				@SuppressWarnings("unused")
				public class X {
				    @NonNullByDefault\s
				    public void foo(@Nullable String [] args) {
				        class local {
				            class Deeply {
				                Object zoo() {
				                    return null; // defaults applying from foo
				                }
				            }
				        };
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				return null; // defaults applying from foo
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// bug 367154 - [compiler][null] Problem in propagating null defaults.
public void test_default_nullness_014() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				
				@SuppressWarnings("unused")
				public class X {
				
				    public void foo(@Nullable String [] args) {
				        @NonNullByDefault
				        class local {
				            class Deeply {
				                Object zoo() {
				                    return null;  // expect error here
				                }
				            }
				        };
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				return null;  // expect error here
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// bug 367154 - [compiler][null] Problem in propagating null defaults.
// initializer involved
public void test_default_nullness_015() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				
				@SuppressWarnings("unused")
				@NonNullByDefault
				public class X {
				    {
				        class local {
				            class Deeply {
				                Object zoo() {
				                    return null;  // expect error here
				                }
				            }
				        };
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				return null;  // expect error here
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}

// default nullness applied to fields, class-level:
public void test_default_nullness_016() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    Object foo;
				    void doFoo() {
				        foo = null;
				    }
				    class Inner {
				        Object iFoo;
				        void diFoo(@Nullable Object arg) {
				            iFoo = arg;
				        }
				    }
				}
				""",
		},
		"----------\n" +
		"1. ERROR in X.java (at line 4)\n" +
		"	Object foo;\n" +
		"	       ^^^\n" +
		"The @NonNull field foo may not have been initialized\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	foo = null;\n" +
		"	      ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 9)\n" +
		"	Object iFoo;\n" +
		"	       ^^^^\n" +
		"The @NonNull field iFoo may not have been initialized\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 11)\n" +
		"	iFoo = arg;\n" +
		"	       ^^^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n");
}

// default nullness applied to fields, method level applied to local class + redundant annotation
public void test_default_nullness_017() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNullByDefault
				    Object doFoo() {
				        class Local {
				            Object foo;
				            @NonNull Object goo;
				        };\
				        return new Local();
				    }
				}
				""",
		},
		options,
		"""
			----------
			1. ERROR in X.java (at line 6)
				Object foo;
				       ^^^
			The @NonNull field foo may not have been initialized
			----------
			2. WARNING in X.java (at line 7)
				@NonNull Object goo;
				^^^^^^^^^^^^^^^
			The nullness annotation is redundant with a default that applies to this location
			----------
			3. ERROR in X.java (at line 7)
				@NonNull Object goo;
				                ^^^
			The @NonNull field goo may not have been initialized
			----------
			""");
}

// package case
public void test_nullness_default_018() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNUSED_WARNING_TOKEN, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"pack/NullWarn.java",
			"""
				package pack;
				@SuppressWarnings("null")
				public class NullWarn {
				
				    // Some code
				
				}
				"""
		},
		customOptions,
		"");

}

// type case (inside default package)
public void test_nullness_default_018b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNUSED_WARNING_TOKEN, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"NullWarn.java",
			"""
				@SuppressWarnings("null")
				public class NullWarn {
				
				    // Some code
				
				}
				"""
		},
		customOptions,
		"");

}

// redundant default annotations - class vs. inner class
public void test_redundant_annotation_01() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
	runWarningTestWithLibs(
		true,
		new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y {
				    @NonNullByDefault class Inner {
				        @NonNullByDefault class DeepInner {}
				    }
				    class Inner2 {
				        @NonNullByDefault class DeepInner2 {
				        }
				        void foo() {
				            @SuppressWarnings("unused") @NonNullByDefault class Local {}
				        }
				    }
				}
				@NonNullByDefault class V {}
				""",
			"p3/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault package p3;\n",
			"p3/Z.java",
			"""
				package p3;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Z {
				}
				class X {
				    @NonNullByDefault class Inner {}
				    class Inner2 {
				        @NonNullByDefault class DeepInner {}
				    }
				}
				"""
		},
		customOptions,
		"""
			----------
			1. WARNING in p2\\Y.java (at line 5)
				@NonNullByDefault class Inner {
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Y
			----------
			2. WARNING in p2\\Y.java (at line 6)
				@NonNullByDefault class DeepInner {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Y.Inner
			----------
			3. WARNING in p2\\Y.java (at line 9)
				@NonNullByDefault class DeepInner2 {
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Y
			----------
			4. WARNING in p2\\Y.java (at line 12)
				@SuppressWarnings("unused") @NonNullByDefault class Local {}
				                            ^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Y
			----------
			----------
			1. WARNING in p3\\Z.java (at line 3)
				@NonNullByDefault
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing package p3
			----------
			2. WARNING in p3\\Z.java (at line 7)
				@NonNullByDefault class Inner {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing package p3
			----------
			3. WARNING in p3\\Z.java (at line 9)
				@NonNullByDefault class DeepInner {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing package p3
			----------
			""");
}

// redundant default annotations - class vs. method
public void test_redundant_annotation_02() {
	Map customOptions = getCompilerOptions();
	runWarningTestWithLibs(
		true,
		new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y {
				    @NonNullByDefault void foo() {}
				}
				class Z {
				    @NonNullByDefault void bar() {
				         @NonNullByDefault @SuppressWarnings("unused") class Zork {
				             @NonNullByDefault void fubar() {}
				         }
				    }
				    @NonNullByDefault void zink() {
				         @SuppressWarnings("unused") class Bork {
				             @NonNullByDefault void jubar() {}
				         }
				    }
				}
				"""
		},
		customOptions,
		"""
			----------
			1. WARNING in p2\\Y.java (at line 5)
				@NonNullByDefault void foo() {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Y
			----------
			2. WARNING in p2\\Y.java (at line 9)
				@NonNullByDefault @SuppressWarnings("unused") class Zork {
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing method bar()
			----------
			3. WARNING in p2\\Y.java (at line 10)
				@NonNullByDefault void fubar() {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Zork
			----------
			4. WARNING in p2\\Y.java (at line 15)
				@NonNullByDefault void jubar() {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing method zink()
			----------
			""");
}

//redundant default annotations - class vs. method - generics
public void test_redundant_annotation_02g() {
	Map customOptions = getCompilerOptions();
	runWarningTestWithLibs(
		true,
		new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y<TY> {
				    @NonNullByDefault <TF> void foo(TF arg) {}
				}
				class Z {
				    @NonNullByDefault <TB> void bar() {
				         @NonNullByDefault @SuppressWarnings("unused") class Zork {
				             @NonNullByDefault void fubar(TB arg) {}
				         }
				    }
				}
				"""
		},
		customOptions,
		"""
			----------
			1. WARNING in p2\\Y.java (at line 5)
				@NonNullByDefault <TF> void foo(TF arg) {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Y<TY>
			----------
			2. WARNING in p2\\Y.java (at line 9)
				@NonNullByDefault @SuppressWarnings("unused") class Zork {
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing method bar()
			----------
			3. WARNING in p2\\Y.java (at line 10)
				@NonNullByDefault void fubar(TB arg) {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type Zork
			----------
			""");
}

// test missing default nullness annotation for types in default package
public void test_missing_default_annotation_01() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"Lib.java",
			"""
				public class Lib {
				    Object getObject() { return new Object(); }
				}
				""",
			"X.java",
			"""
				public class X {
					 class XInner{}
				    Object getObject(Lib l) {
				        return l.getObject();
				    }
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in Lib.java (at line 1)
				public class Lib {
				             ^^^
			A default nullness annotation has not been specified for the type Lib
			----------
			----------
			1. ERROR in X.java (at line 1)
				public class X {
				             ^
			A default nullness annotation has not been specified for the type X
			----------
			""");
}

// test missing default nullness annotation for a package with package-info
public void test_missing_default_annotation_02() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			"p2/package-info.java",
			"package p2;\n",
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y {
				   void foo() {}
				}
				""",
			"p3/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault package p3;\n",
			"p3/Z.java",
			"""
				package p3;
				import org.eclipse.jdt.annotation.*;
				public class Z {
				    @NonNullByDefault void bar() {}
				}
				""",
		},
		customOptions,
		"""
			----------
			1. ERROR in p2\\package-info.java (at line 1)
				package p2;
				        ^^
			A default nullness annotation has not been specified for the package p2
			----------
			----------
			1. WARNING in p3\\Z.java (at line 4)
				@NonNullByDefault void bar() {}
				^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing package p3
			----------
			""");
}

// redundant default annotations - class vs. inner class
// ensure that disabling null annotations also disables this diagnostic
public void test_redundant_annotation_04() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.DISABLED);
	runConformTestWithLibs(
		new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Y {
				    @NonNullByDefault class Inner {
				        @NonNullByDefault class DeepInner {}
				    }
				    class Inner2 {
				        @NonNullByDefault class DeepInner2 {
				        }
				        @NonNullByDefault void foo(@Nullable @NonNull Object arg) {
				            @NonNullByDefault class Local {}
				        }
				    }
				}
				@NonNullByDefault class V {}
				""",
			"p3/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault package p3;\n",
			"p3/Z.java",
			"""
				package p3;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Z {
				}
				class X {
				    @NonNullByDefault class Inner {}
				    class Inner2 {
				        @NonNullByDefault class DeepInner {}
				    }
				}
				"""
		},
		customOptions,
		"");
}

// contradictory null annotations
public void test_contradictory_annotations_01() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				public class Y {
				    void foo(@NonNull @Nullable Object o) {}
				    @Nullable @NonNull Object bar() {
				        @NonNull @Nullable Object o = null;
				        return o;
				    }
				}
				class Z {
				    @NonNullByDefault void bar() {}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in p2\\Y.java (at line 4)
				void foo(@NonNull @Nullable Object o) {}
				                  ^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			2. ERROR in p2\\Y.java (at line 5)
				@Nullable @NonNull Object bar() {
				          ^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			3. ERROR in p2\\Y.java (at line 6)
				@NonNull @Nullable Object o = null;
				         ^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			""");
}

// contradictory null annotations on a field
public void test_contradictory_annotations_02() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				public class Y {
				    @NonNull @Nullable Object o;
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in p2\\Y.java (at line 4)
				@NonNull @Nullable Object o;
				         ^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			""");
}

// contradictory null annotations on a field - array type
public void test_contradictory_annotations_03() {
	Map customOptions = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				public class Y {
				    @NonNull @Nullable Object[] o;
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in p2\\Y.java (at line 4)
				@NonNull @Nullable Object[] o;
				         ^^^^^^^^^
			Contradictory null specification; only one of @NonNull and @Nullable can be specified at any location
			----------
			""");
}

// a nonnull variable is dereferenced in a loop
public void test_nonnull_var_in_constrol_structure_1() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
// This option currently does nothing:
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    void print4(@NonNull String s) {
				        for (int i=0; i<4; i++)
				             print(s);
				    }
				    void print5(@Nullable String s) {
				        for (int i=0; i<5; i++)
				             print(s);
				    }
				    void print6(boolean b) {
				        String s = b ? null : "";
				        for (int i=0; i<5; i++)
				             print(s);
				    }
				    void print(@NonNull String s) {
				        System.out.print(s);
				    }
				}
				""",

		},
		customOptions,
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	void print4(@NonNull String s) {\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
	  ? "	            ^^^^^^^^^^^^^^^^^\n"
	  : "	            ^^^^^^^^^^^^^^^\n"
		) +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		mismatch_NonNull_Nullable("String") +
		"----------\n" +
		"3. ERROR in X.java (at line 15)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
		"----------\n" +
		"4. WARNING in X.java (at line 17)\n" +
		"	void print(@NonNull String s) {\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
	  ? "	           ^^^^^^^^^^^^^^^^^\n"
	  : "	           ^^^^^^^^^^^^^^^\n"
		) +
		"The nullness annotation is redundant with a default that applies to this location\n" +
		"----------\n");
}
// a nonnull variable is dereferenced in a finally block
public void test_nonnull_var_in_constrol_structure_2() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
// This option currently does nothing:
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;\
				@NonNullByDefault
				public class X {
				    void print4(String s) {
				        try { /*empty*/ } finally {
				             print(s);
				        }
				    }
				    void print5(@Nullable String s) {
				        try { /*empty*/ } finally {
				             print(s);
				        }
				    }
				    void print6(boolean b) {
				        String s = b ? null : "";
				        try { /*empty*/ } finally {
				             print(s);
				        }
				    }
				    void print(String s) {
				        System.out.print(s);
				    }
				}
				""",

		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 10)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		mismatch_NonNull_Nullable("String") +
		"----------\n" +
		"2. ERROR in X.java (at line 16)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
		"----------\n");
}
// a nonnull variable is dereferenced in a finally block inside a loop
public void test_nonnull_var_in_constrol_structure_3() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_ANNOTATION, JavaCore.IGNORE);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void print4(@NonNull String s) {
				        for (int i=0; i<4; i++)
				            try { /*empty*/ } finally {
				                 print(s);
				            }
				    }
				    void print5(@Nullable String s) {
				        for (int i=0; i<5; i++)
				            try { /*empty*/ } finally {
				                 print(s);
				            }
				    }
				    void print6(boolean b) {
				        String s = b ? null : "";
				        for (int i=0; i<4; i++)
				            try { /*empty*/ } finally {
				                 print(s);
				            }
				    }
				    void print(@NonNull String s) {
				        System.out.print(s);
				    }
				}
				""",

		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		mismatch_NonNull_Nullable("String") +
		"----------\n" +
		"2. ERROR in X.java (at line 19)\n" +
		"	print(s);\n" +
		"	      ^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
		"----------\n");
}
// witness for an AIOOBE in FlowContext.recordExpectedType()
public void test_message_send_in_control_structure_01() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.IGNORE);
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.WARNING);
	runNegativeTestWithLibs(
		new String[] {
			"p/Scope.java",
			"""
				package p;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public abstract class Scope {
					public ReferenceBinding findMemberType(char[] typeName, ReferenceBinding enclosingType) {
						ReferenceBinding enclosingSourceType = enclosingSourceType();
						PackageBinding currentPackage = getCurrentPackage();
						CompilationUnitScope unitScope = compilationUnitScope();
						ReferenceBinding memberType = enclosingType.getMemberType(typeName);
						ReferenceBinding currentType = enclosingType;
						ReferenceBinding[] interfacesToVisit = null;
						while (true) {
							ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
							if (itsInterfaces != null) {
								if (interfacesToVisit == null) {
									interfacesToVisit = itsInterfaces;
								}
							}
							unitScope.recordReference(currentType, typeName);
						\t
							if ((memberType = currentType.getMemberType(typeName)) != null) {
								if (enclosingSourceType == null
									? memberType.canBeSeenBy(currentPackage)
									: memberType.canBeSeenBy(enclosingType, enclosingSourceType)) {
										return memberType;
								}
							}
						}
					}
					private CompilationUnitScope compilationUnitScope() {
						return compilationUnitScope();
					}
					private PackageBinding getCurrentPackage() {
						return getCurrentPackage();
					}
					private ReferenceBinding enclosingSourceType() {
						return enclosingSourceType();
					}
				}
				""",
			"p/CompilationUnitScope.java",
			"""
				package p;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class CompilationUnitScope {
				    void recordReference(ReferenceBinding rb, char[] name) {}
				}
				""",
			"p/PackageBinding.java",
			"""
				package p;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class PackageBinding {
				}
				""",
			"p/ReferenceBinding.java",
			"""
				package p;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class ReferenceBinding {
				    ReferenceBinding getMemberType(char[] name) { return this; }
				    ReferenceBinding[] superInterfaces() { return new ReferenceBinding[0]; }
				    boolean canBeSeenBy(PackageBinding ob) { return true; }
				    boolean canBeSeenBy(ReferenceBinding rb, ReferenceBinding rb2) { return true; }
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in p\\Scope.java (at line 13)
				if (itsInterfaces != null) {
				    ^^^^^^^^^^^^^
			Redundant null check: The variable itsInterfaces cannot be null at this location
			----------
			2. ERROR in p\\Scope.java (at line 20)
				if ((memberType = currentType.getMemberType(typeName)) != null) {
				    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Redundant null check: The variable memberType cannot be null at this location
			----------
			3. ERROR in p\\Scope.java (at line 21)
				if (enclosingSourceType == null
				    ^^^^^^^^^^^^^^^^^^^
			Null comparison always yields false: The variable enclosingSourceType cannot be null at this location
			----------
			""");
}

// Bug 370930 - NonNull annotation not considered for enhanced for loops
public void test_message_send_in_control_structure_02() {
	runWarningTestWithLibs(
		true, // flush
		new String[] {
			"Bug370930.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				public class Bug370930 {
					void loop(Collection<String> list) {
						for(@NonNull String s: list) { // warning here: insufficient info on elements
							expectNonNull(s); // no warning here
						}
					}
				\t
					void expectNonNull(@NonNull String s) {}
				}
				"""
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Bug370930.java (at line 5)\n" +
		"	for(@NonNull String s: list) { // warning here: insufficient info on elements\n" +
		"	                       ^^^^\n" +
		nullTypeSafety() + "The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" +
		"----------\n");
}
//Bug 370930 - NonNull annotation not considered for enhanced for loops over array
public void test_message_send_in_control_structure_02a() {
	runWarningTestWithLibs(
		true, // flush
		new String[] {
			"Bug370930.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Bug370930 {
					void loop(String[] array) {
						for(@NonNull String s: array) { // warning here: insufficient info on elements
							expectNonNull(s); // no warning here
						}
					}
				\t
					void expectNonNull(@NonNull String s) {}
				}
				"""
		},
		getCompilerOptions(),
		"----------\n" +
		"1. WARNING in Bug370930.java (at line 4)\n" +
		"	for(@NonNull String s: array) { // warning here: insufficient info on elements\n" +
		"	                       ^^^^^\n" +
		nullTypeSafety() + "The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" +
		"----------\n");
}
//Bug 370930 - NonNull annotation not considered for enhanced for loops
public void test_message_send_in_control_structure_03() {
	runNegativeTestWithLibs(
		new String[] {
			"Bug370930.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import java.util.*;
				public class Bug370930 {
					void loop(Collection<String> list) {
						for(@Nullable String s: list) {
							expectNonNull(s); // warning here
						}
					}
				\t
					void expectNonNull(@NonNull String s) {}
				}
				"""
		},
		"----------\n" +
		"1. ERROR in Bug370930.java (at line 6)\n" +
		"	expectNonNull(s); // warning here\n" +
		"	              ^\n" +
		mismatch_NonNull_Nullable("String") +
		"----------\n");
}
public void test_assignment_expression_1() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@Nullable Object foo() {
						Object o = null;
						boolean keepLooking = true;
						while(keepLooking) {
							if ((o=getO()) != null) {
								return o;
							}
						}
						return null;
					}
				
					private @Nullable Object getO() {
						return new Object();
					}
				}
				""",

		},
		customOptions,
		"");
}
// a nonnull variable is dereferenced in a method of a nested type
public void test_nesting_1() {
	Map customOptions = getCompilerOptions();
//	customOptions.put(CompilerOptions.OPTION_ReportPotentialNullSpecViolation, JavaCore.ERROR);
// This option currently does nothing:
//	customOptions.put(JavaCore.COMPILER_NONNULL_IS_DEFAULT, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    void print4(final String s1) {
				        for (int i=0; i<3; i++)
				            new Runnable() {
				                public void run() {
				                     print(s1);
				                }
				            }.run();
				    }
				    void print8(final @Nullable String s2) {
				        for (int i=0; i<3; i++)
				            new Runnable() {
				                public void run() {
				                     print(s2);
				                }
				            }.run();
				    }
				    void print16(boolean b) {
				        final String s3 = b ? null : "";
				        for (int i=0; i<3; i++)
				            new Runnable() {
				                public void run() {
				                     @NonNull String s3R = s3;
				                }
				            }.run();
				    }
				    void print(String s) {
				        System.out.print(s);
				    }
				}
				""",

		},
		customOptions,
		"----------\n" +
		"1. ERROR in X.java (at line 16)\n" +
		"	print(s2);\n" +
		"	      ^^\n" +
		mismatch_NonNull_Nullable("String") +
		"----------\n" +
		"2. ERROR in X.java (at line 25)\n" +
		"	@NonNull String s3R = s3;\n" +
		"	                      ^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is inferred as @Nullable\n" +
		"----------\n");
}
// Test a regression incurred to the OT/J based implementation
// by the fix in Bug 360328 - [compiler][null] detect null problems in nested code (local class inside a loop)
public void test_constructor_with_nested_class() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
				    final Object o1;
				    final Object o2;
				    public X() {
				         this.o1 = new Object() {
				             @Override
				             public String toString() { return "O1"; }
				         };
				         this.o2 = new Object();\
				    }
				}
				"""
		},
		null,//options
		"");
}
// test analysis disablement, binary type contains annotation
public void test_options_01() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
			new String[] {
				"ContainingInner2.java",
				"""
					public class ContainingInner2 {
					    public ContainingInner2 (@org.eclipse.jdt.annotation.NonNull Object o) {
					    }
					    public class Inner {
					        public <T> Inner (@org.eclipse.jdt.annotation.NonNull T o) {
					        }
					    }
					}
					""",
			},
			null /*customOptions*/,
			"");
	customOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.DISABLED);
	runConformTestWithLibs(
		false, // flush directory
		new String[] {
			"X.java",
			"""
				public class X {
					 void create() {
				          ContainingInner2 container = new ContainingInner2(null);
					       ContainingInner2.Inner inner = container.new Inner(null);
				    }
				}
				"""},
		customOptions,
		""  /* compiler output */);
}
// test illegally trying to ignore null spec violations
public void test_options_02() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.IGNORE); // has no effect
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    public void foo(@org.eclipse.jdt.annotation.NonNull Object o) {
				        o = null;
				        Object p = o;
				        if (p == null)
				            p.toString();
				    }
				}
				""",
		},
		customOptions,
		"""
			----------
			1. ERROR in Test.java (at line 3)
				o = null;
				    ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in Test.java (at line 5)
				if (p == null)
				    ^
			Null comparison always yields false: The variable p cannot be null at this location
			----------
			3. WARNING in Test.java (at line 6)
				p.toString();
				^^^^^^^^^^^^
			Dead code
			----------
			""");
}
// test setting null spec violations to "warning"
public void test_options_03() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING); // OK
	runNegativeTestWithLibs(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    public void foo(@org.eclipse.jdt.annotation.NonNull Object o) {
				        o = null;
				        Object p = o;
				        if (p == null)
				            p.toString();
				    }
				}
				""",
		},
		customOptions,
		"""
			----------
			1. WARNING in Test.java (at line 3)
				o = null;
				    ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in Test.java (at line 5)
				if (p == null)
				    ^
			Null comparison always yields false: The variable p cannot be null at this location
			----------
			3. WARNING in Test.java (at line 6)
				p.toString();
				^^^^^^^^^^^^
			Dead code
			----------
			""");
}
// access to a non-null field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_1() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object o = new Object();
				    public String oString() {
				         return o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"");
}

// a non-null field is not properly initialized
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object o;
				    public String oString() {
				         return o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 3)
				@NonNull Object o;
				                ^
			The @NonNull field o may not have been initialized
			----------
			""");
}

// a non-null field is not properly initialized - explicit constructor
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2a() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object o;
				    X (boolean b) { // only potentially initialized
				        if (b)
				            o = this;
				    }
				    X (@NonNull Object other) {
				        o = other;
				    }
				    public String oString() {
				        return o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				X (boolean b) { // only potentially initialized
				^^^^^^^^^^^^^
			The @NonNull field o may not have been initialized
			----------
			""");
}

// a non-null field is not properly initialized - explicit constructor - incomplete switch
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				enum Color { BLACK, GREEN }
				public class X {
				    @NonNull Object o;
				    X (Color c) { // only potentially initialized
				        switch (c) {
				            case BLACK: o = this; break;
				            case GREEN: o = new Object(); break;
				        }
				    }
				    public String oString() {
				        return o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 5)
				X (Color c) { // only potentially initialized
				^^^^^^^^^^^
			The @NonNull field o may not have been initialized. Note that a problem regarding missing \'default:\' on \'switch\' has been suppressed, which is perhaps related to this problem
			----------
			""");
}

// a non-null static field is not properly initialized
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2c() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    static @NonNull Object o;
				    static {
				        if (new Object().hashCode() == 42)
				            o = new Object();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 3)
				static @NonNull Object o;
				                       ^
			The @NonNull field o may not have been initialized
			----------
			""");
}

// a non-null static field is properly initialized
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2d() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    static @NonNull Object o;
				    static {
				         o = new Object();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"");
}

// a non-null field is properly initialized - using this.f reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_2e() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X<T> {
				    @NonNull Object f;
				    {
				         this.f = new Object();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"");
}

// a non-null field is initialized to null
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_3() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object o = null;
				    public String oString() {
				         return o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 3)
				@NonNull Object o = null;
				                    ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// a non-null field is assigned to null
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_4() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object o = new Object();
				    void breakIt1() {
				         o = null;
				    }
				    void breakIt2() {
				         this.o = null;
				    }
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 5)
				o = null;
				    ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in X.java (at line 8)
				this.o = null;
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// a non-null field is checked for null
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_5() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object o = new Object();
				    boolean checkIt1() {
				         return o == null;
				    }
				    boolean checkIt() {
				         return this.o != null;
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return o == null;\n" +
		"	       ^\n" +
		checkAlwaysFalse_nonnull("The field o", "Object") +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	return this.o != null;\n" +
		"	            ^\n" +
		redundant_check_nonnull("The field o", "@NonNull Object") +
		"----------\n");
}

// a non-null field is checked for null twice - method call inbetween
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_6() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Object o = new Object();
				    boolean checkIt1() {
				         if (o != null)
				             System.out.print("not null");
				         System.out.print("continue");
				         return this.o == null;
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	if (o != null)\n" +
		"	    ^\n" +
		redundant_check_nonnull("The field o", "@NonNull Object") +
		"----------\n" +
		"2. ERROR in X.java (at line 8)\n" +
		"	return this.o == null;\n" +
		"	            ^\n" +
		checkAlwaysFalse_nonnull("The field o", "Object") +
		"----------\n");
}

// a non-null field is accessed via a qualified name reference - static field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_7() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Objects {
				    static @NonNull Object o = new Object();
				}
				public class X {
				    @NonNull Object getIt1() {
				         if (Objects.o != null) // redundant
				             System.out.print("not null");
				         System.out.print("continue");
				         return Objects.o;
				    }
				    @NonNull Object getIt2() {
				         if (null != Objects.o) // redundant
				             System.out.print("not null");
				         System.out.print("continue");
				         return Objects.o;
				    }
				    String getIt3() {
				         return Objects.o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (Objects.o != null) // redundant\n" +
		"	            ^\n" +
		redundant_check_nonnull("The field o", "@NonNull Object") +
		"----------\n" +
		"2. ERROR in X.java (at line 13)\n" +
		"	if (null != Objects.o) // redundant\n" +
		"	                    ^\n" +
		redundant_check_nonnull("The field o", "@NonNull Object") +
		"----------\n");
}

// a non-null field is accessed via a qualified name reference - instance field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_8() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Objects {
				    @NonNull Object o = new Object();
				}
				public class X {
				    @NonNull Object getIt1(@NonNull Objects objs) {
				         if (objs.o == null) // always false
				             System.out.print("not null");
				         System.out.print("continue");
				         return objs.o;
				    }
				    String getIt2(@NonNull Objects objs) {
				         return objs.o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	if (objs.o == null) // always false\n" +
		"	         ^\n" +
		checkAlwaysFalse_nonnull("The field o", "Object") +
		"----------\n" +
		"2. WARNING in X.java (at line 8)\n" +
		"	System.out.print(\"not null\");\n" +
		"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Dead code\n" +
		"----------\n");
}

// a non-null field is accessed via an indirect field reference - instance field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_9() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Objects {
				    @NonNull Object o = new Object();
				}
				public class X {
				    Objects objs = new Objects();
				    @NonNull Object getIt1() {
				         if (this.objs.o != null) // redundant
				             System.out.print("not null");
				         System.out.print("continue");
				         if (getObjs().o != null) // redundant
				             System.out.print("not null");
				         return this.objs.o;
				    }
				    Objects getObjs() { return this.objs; }
				    String getIt2() {
				         return this.objs.o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	if (this.objs.o != null) // redundant\n" +
		"	              ^\n" +
		redundant_check_nonnull("The field o", "@NonNull Object") +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	if (getObjs().o != null) // redundant\n" +
		"	              ^\n" +
		redundant_check_nonnull("The field o", "@NonNull Object") +
		"----------\n");
}

// trying to assign null to a nonnull field via a single / a qualified name reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_11() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Objects {
				    @NonNull Object o = new Object();
				    void test0(@Nullable Object x) {
				         o = x;
				    }
				}
				public class X {
				    void test(@NonNull Objects objs) {
				         objs.o = null;
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	o = x;\n" +
		"	    ^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	objs.o = null;\n" +
		"	         ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n");
}

// @NonNull is applied to a field with primitive type
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_12() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull int o = 1;
				}
				"""
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 3)
				@NonNull int o = 1;
				^^^^^^^^
			The nullness annotation @NonNull is not applicable for the primitive type int
			----------
			""");
}

// A final field is initialized to non-null, treat as effectively @NonNull
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void _test_nonnull_field_13() {
	// withdrawn as of https://bugs.eclipse.org/331649#c75
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    final String s1 = "";
				    @NonNull String s2;
				    X() {
				        s2 = s1;
				    }
				}
				"""
		},
		null /*customOptions*/,
		"");
}

// A field in a different CU is implicitly @NonNull (by type default) - that class is read from binary
// Assignment to other @NonNull field should not raise a warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_14() {
	runConformTestWithLibs(
		new String[] {
			"p1/X.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class X {
				    public String s1 = "";
				}
				""",
		},
		null /*customOptions*/,
		"");
	runConformTestWithLibs(
			false /*flush*/,
			new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				import p1.X;
				public class Y {
				    @NonNull String s2 = "";
				    void foo(X other) {
				        s2 = other.s1;
				    }
				}
				"""
		},
		null /*customOptions*/,
		"");
}

// A field in a different CU is implicitly @NonNull (by package default) - that class is read from binary
// Assignment to other @NonNull field should not raise a warning
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nonnull_field_14b() {
	runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			"package p1;\n",
			"p1/X.java",
			"""
				package p1;
				public class X {
				    public String s1 = "";
				}
				""",
		},
		null /*customOptions*/,
		"");
	runConformTestWithLibs(
			false /*flush*/,
			new String[] {
			"p2/Y.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				import p1.X;
				public class Y {
				    @NonNull String s2 = "";
				    void foo(X other) {
				        s2 = other.s1;
				    }
				}
				"""
		},
		null /*customOptions*/,
		"");
}

// A @NonNull field is assumed to be initialized by the injection framework
// [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400421
public void test_nonnull_field_15() {
	runConformTestWithLibs(
		new String[] {
			GOOGLE_INJECT_NAME,
			GOOGLE_INJECT_CONTENT,
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import com.google.inject.Inject;
				public class X {
				    @NonNull @Inject Object o;
				    @NonNullByDefault class Inner {
				        @Inject String s;
				    }
				}
				""",
		},
		null /*customOptions*/,
		"");
}

// Injection is optional, don't rely on the framework
// [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400421
public void test_nonnull_field_16() {
	runNegativeTestWithLibs(
		new String[] {
			GOOGLE_INJECT_NAME,
			GOOGLE_INJECT_CONTENT,
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import com.google.inject.Inject;
				public class X {
				    @Inject(optional=true) @NonNull Object o;
				    @NonNullByDefault class Inner {
				        @Inject(optional=true) String s;
				        @Inject(optional=false) String t;
				    }
				}
				""",
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				@Inject(optional=true) @NonNull Object o;
				                                       ^
			The @NonNull field o may not have been initialized
			----------
			2. ERROR in X.java (at line 6)
				@Inject(optional=true) String s;
				                              ^
			The @NonNull field s may not have been initialized
			----------
			""");
}

// Using jakarta.inject.Inject, slight variations
// [compiler] Null analysis for fields does not take @com.google.inject.Inject into account
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400421
public void test_nonnull_field_17() {
	runNegativeTestWithLibs(
		new String[] {
			JAVAX_INJECT_NAME,
			JAVAX_INJECT_CONTENT,
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import jakarta.inject.Inject;
				public class X {
				    @NonNull @Inject static String s; // warn since injection of static field is less reliable
				    @NonNull @Inject @Deprecated Object o;
				    public X() {}
				}
				""",
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				@NonNull @Inject static String s; // warn since injection of static field is less reliable
				                               ^
			The @NonNull field s may not have been initialized
			----------
			""");
}

//Using jakarta.inject.Inject
//jakarta.inject.Inject not treated properly with annotation-based null analysis
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1112
public void test_nonnull_field_18() {
	runConformTestWithLibs(
		new String[] {
			JAKARTA_INJECT_NAME,
			JAKARTA_INJECT_CONTENT,
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import jakarta.inject.Inject;
				public class X {
				    @NonNull @Inject Object o;
				    @NonNullByDefault class Inner {
				        @Inject String s;
				    }
				}
				""",
		},
		null /*customOptions*/,
		"");
}

//Using jakarta.inject.Inject, slight variations
//jakarta.inject.Inject not treated properly with annotation-based null analysis
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1112
public void test_nonnull_field_19() {
	runNegativeTestWithLibs(
		new String[] {
			JAKARTA_INJECT_NAME,
			JAKARTA_INJECT_CONTENT,
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import jakarta.inject.Inject;
				public class X {
				    @NonNull @Inject static String s; // warn since injection of static field is less reliable
				    @NonNull @Inject @Deprecated Object o;
				    public X() {}
				}
				""",
		},
		null /*customOptions*/,
		"""
			----------
			1. ERROR in X.java (at line 4)
				@NonNull @Inject static String s; // warn since injection of static field is less reliable
				                               ^
			The @NonNull field s may not have been initialized
			----------
			""");
}

// access to a nullable field - field reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_1() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o = new Object();
				    public String oString() {
				         return this.o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return this.o.toString();\n" +
		"	            ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}
// access to a nullable field - single name reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_2() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o = new Object();
				    public String oString() {
				         return o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return o.toString();\n" +
		"	       ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}
// access to a nullable field - qualified name reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_3() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o = new Object();
				    @Nullable X other;
				    public String oString() {
				         return other.o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	return other.o.toString();\n" +
		"	       ^^^^^\n" +
		potNPE_nullable("The field other") +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	return other.o.toString();\n" +
		"	             ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}
// access to a nullable field - qualified name reference - multiple segments
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_3m() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o = new Object();
				    @Nullable X other;
				    public String oString() {
				         return other.other.o.toString();
				    }
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	return other.other.o.toString();\n" +
		"	       ^^^^^\n" +
		potNPE_nullable("The field other") +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	return other.other.o.toString();\n" +
		"	             ^^^^^\n" +
		potNPE_nullable("The field other") +
		"----------\n" +
		"3. ERROR in X.java (at line 6)\n" +
		"	return other.other.o.toString();\n" +
		"	                   ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}
// access to a nullable field - dereference after check
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_4() {
	// currently no flow analysis for fields is implemented,
	// but the direct sequence of null-check + dereference is optionally supported as a special case
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o = new Object();
				    public String oString() {
				         if (this.o != null)
				             return this.o.toString();
				         if (o != null)
				             return o.toString();
				         return "";
				    }
				    public String oString2() {
				         String local = o.toString();
				         if (this.o != null) {
				             this.toString();
				             return this.o.toString(); // warn here
				         }
				         return "";
				    }
				}
				"""
		},
		options /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	String local = o.toString();\n" +
		"	               ^\n" +
		potNPE_nullable("The field o") +
		"----------\n" +
		"2. ERROR in X.java (at line 15)\n" +
		"	return this.o.toString(); // warn here\n" +
		"	            ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}

// access to a nullable field - intermediate component in a QNR
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_5() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @NonNull Y y = new Y();
				    public String oString() {
				         return y.z.o.toString(); // pot.NPE on z
				    }
				}
				""",
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Y {
				    @Nullable Z z = new Z();
				}
				""",
			"Z.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Z {
				    @NonNull Object o = new Object();
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return y.z.o.toString(); // pot.NPE on z\n" +
		"	         ^\n" +
		potNPE_nullable("The field z") +
		"----------\n");
}

// access to a nullable field - intermediate component in a QNR - inverse of test_nullable_field_5
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_6() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Y y = new Y();
				    public String oString() {
				         return y.z.o.toString(); // pot.NPE on y and o
				    }
				}
				""",
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Y {
				    @NonNull Z z = new Z();
				}
				""",
			"Z.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Z {
				    Object dummy;
				    @Nullable Object o = new Object();
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return y.z.o.toString(); // pot.NPE on y and o\n" +
		"	       ^\n" +
		potNPE_nullable("The field y") +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	return y.z.o.toString(); // pot.NPE on y and o\n" +
		"	           ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}

// access to a nullable field - intermediate component in a double field reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_7() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Y y = new Y();
				    public String oString() {
				         return this.y.o.toString(); // pot.NPE on y and o
				    }
				}
				""",
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Y {
				    @Nullable Object o = new Object();
				}
				"""
		},
		null /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	return this.y.o.toString(); // pot.NPE on y and o\n" +
		"	            ^\n" +
		potNPE_nullable("The field y") +
		"----------\n" +
		"2. ERROR in X.java (at line 5)\n" +
		"	return this.y.o.toString(); // pot.NPE on y and o\n" +
		"	              ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}

// static access to a nullable field - qualified name reference
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_8() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable static final Object o = null;
				    public void foo() {
				         if (X.o == null){
								System.out.println(X.o);
						  }
				    }
				}
				"""
		},
		null /*customOptions*/,
		"");
}

// illegal use of @Nullable for a field of primitive type
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_9() {
	runNegativeTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					public class X {
					    @Nullable int i;
					}
					"""
			},
			null /*customOptions*/,
			"""
				----------
				1. ERROR in X.java (at line 3)
					@Nullable int i;
					^^^^^^^^^
				The nullness annotation @Nullable is not applicable for the primitive type int
				----------
				""");
}

// protected access to nullable fields - different kinds of references
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o1, o2, o3;
				    @NonNull X x = new X();
				    public void foo(X other) {
				         if (other.o1 != null){
				             System.out.println(other.o1.toString());
				         }
				         if (this.o2 != null)
				             System.out.println(o2.toString());
				         if (this.o2 != null)
				             System.out.println(this.o2.toString());
				         System.out.println (null != o3 ? o3.toString() : "nothing");
				         if (this.x.o1 != null)
				             System.out.println(x.o1.toString());
				         if (x.o1 != null)
				             System.out.println(this.x.o1.toString());
				         if (this.x.o1 != null)
				             System.out.println(this.x.o1.toString());
				    }
				}
				"""
		},
		options,
		"");
}

// protected access to nullable fields - different kinds of references - option not enabled
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10b() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.DISABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o1, o2, o3;
				    @NonNull X x = new X();
				    public void foo(X other) {
				         if (other.o1 != null){
				             System.out.println(other.o1.toString());
				         }
				         if (this.o2 != null)
				             System.out.println(o2.toString());
				         if (this.o2 != null)
				             System.out.println(this.o2.toString());
				         System.out.println (null != o3 ? o3.toString() : "nothing");
				         if (this.x.o1 != null)
				             System.out.println(x.o1.toString());
				         if (x.o1 != null)
				             System.out.println(this.x.o1.toString());
				         if (this.x.o1 != null)
				             System.out.println(this.x.o1.toString());
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	System.out.println(other.o1.toString());\n" +
		"	                         ^^\n" +
		potNPE_nullable("The field o1") +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	System.out.println(o2.toString());\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o2") +
		"----------\n" +
		"3. ERROR in X.java (at line 12)\n" +
		"	System.out.println(this.o2.toString());\n" +
		"	                        ^^\n" +
		potNPE_nullable("The field o2") +
		"----------\n" +
		"4. ERROR in X.java (at line 13)\n" +
		"	System.out.println (null != o3 ? o3.toString() : \"nothing\");\n" +
		"	                                 ^^\n" +
		potNPE_nullable("The field o3") +
		"----------\n" +
		"5. ERROR in X.java (at line 15)\n" +
		"	System.out.println(x.o1.toString());\n" +
		"	                     ^^\n" +
		potNPE_nullable("The field o1") +
		"----------\n" +
		"6. ERROR in X.java (at line 17)\n" +
		"	System.out.println(this.x.o1.toString());\n" +
		"	                          ^^\n" +
		potNPE_nullable("The field o1") +
		"----------\n" +
		"7. ERROR in X.java (at line 19)\n" +
		"	System.out.println(this.x.o1.toString());\n" +
		"	                          ^^\n" +
		potNPE_nullable("The field o1") +
		"----------\n");
}

// protected access to nullable fields - different boolean operators
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10c() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o1, o2, o3;
				    public void foo(X other) {
				         if (o1 != null && o2 != null & o3 != null)\s
				             System.out.println(o2.toString());
				         if (o1 != null || o2 != null || o3 != null)\s
				             System.out.println(o2.toString()); // warn here: disjunktion is no protection
				         if (!(o1 != null))\s
				             System.out.println(o1.toString()); // warn here: negated inequality is no protection
				         if (!(o1 == null || o2 == null))\s
				             System.out.println(o1.toString()); // don't warn here
				         if (!(o1 == null && o2 == null))\s
				             System.out.println(o2.toString()); // warn here: negated conjunction is no protection
				         if (!(!(o1 == null)))\s
				             System.out.println(o1.toString()); // warn here: double negation is no protection
				         if (!(!(o1 != null && o2 != null)))\s
				             System.out.println(o1.toString()); // don't warn here
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	System.out.println(o2.toString()); // warn here: disjunktion is no protection\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o2") +
		"----------\n" +
		"2. ERROR in X.java (at line 10)\n" +
		"	System.out.println(o1.toString()); // warn here: negated inequality is no protection\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o1") +
		"----------\n" +
		"3. ERROR in X.java (at line 14)\n" +
		"	System.out.println(o2.toString()); // warn here: negated conjunction is no protection\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o2") +
		"----------\n" +
		"4. ERROR in X.java (at line 16)\n" +
		"	System.out.println(o1.toString()); // warn here: double negation is no protection\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o1") +
		"----------\n");
}

// protected access to nullable fields - assignment as expression
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10d() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o1;
				    public void foo(@NonNull X other, X last) {
				         o1 = other;
				         if (o1 == last)\s
				             System.out.println(o1.toString());
				         if ((o1 = other) == last)\s
				             System.out.println(o1.toString());
				         if ((o1 = other) == last) {
				             o1 = null;
				             System.out.println(o1.toString()); // info is expired
				         }
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	System.out.println(o1.toString()); // info is expired\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o1") +
		"----------\n");
}

// protected access to nullable fields - distinguish local and field
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10e() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Y {
				    @Nullable Object o2;
				    void bar(Object o2) {
				        if (o2 != null)
				            System.out.println(this.o2.toString()); // field access is not protected
				    }
				}
				public class X {
				    @NonNull Y o1 = new Y();
				    public void foo() {
				         Y o1 = new Y();
				         if (o1.o2 != null)\s
				             System.out.println(this.o1.o2.toString()); // field access via other field not protected
				         if (this.o1.o2 != null)\s
				             System.out.println(o1.o2.toString()); // field access via local not protected
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. WARNING in X.java (at line 4)\n" +
		"	void bar(Object o2) {\n" +
		"	                ^^\n" +
		"The parameter o2 is hiding a field from type Y\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	System.out.println(this.o2.toString()); // field access is not protected\n" +
		"	                        ^^\n" +
		potNPE_nullable("The field o2") +
		"----------\n" +
		"3. WARNING in X.java (at line 12)\n" +
		"	Y o1 = new Y();\n" +
		"	  ^^\n" +
		"The local variable o1 is hiding a field from type X\n" +
		"----------\n" +
		"4. ERROR in X.java (at line 14)\n" +
		"	System.out.println(this.o1.o2.toString()); // field access via other field not protected\n" +
		"	                           ^^\n" +
		potNPE_nullable("The field o2") +
		"----------\n" +
		"5. ERROR in X.java (at line 16)\n" +
		"	System.out.println(o1.o2.toString()); // field access via local not protected\n" +
		"	                      ^^\n" +
		potNPE_nullable("The field o2") +
		"----------\n");
}

// protected access to nullable fields - duplicate comparison
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_10f() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o1;
				    public void foo(X other) {
				         if (o1 != null && o1 != null) // second term is redundant
				             System.out.println(o1.toString());
				         if (o1 != null)
				             if (o1 != null) // this if is redundant
				                 System.out.println(o1.toString());
				    }
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in X.java (at line 5)
				if (o1 != null && o1 != null) // second term is redundant
				                  ^^
			Redundant null check: The field o1 cannot be null at this location (ignoring concurrency)
			----------
			2. ERROR in X.java (at line 8)
				if (o1 != null) // this if is redundant
				    ^^
			Redundant null check: The field o1 cannot be null at this location (ignoring concurrency)
			----------
			""");
}

// combined test from comment 20 in https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_11() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					class X {
					    @Nullable Object o;
					    public @NonNull Object foo(X x) {
					    	return  x.o != null ? x.o : new Object();
						 }
					    public void goo(X x) {
					    	if (x.o != null) {
					    		x.o.toString();
					    	}
					    }
					    public void boo(X x) {
					    	if (x.o instanceof String) {
					    		x.o.toString();
					    	}
					    }
					    public void zoo(X x) {
					    	x.o = new Object();
					    	System.out.println("hashCode of new Object = " + x.o.hashCode());
					    }
					    public void doo(X x) {
					    	x.o = foo(x); // foo is guaranteed to return @NonNull Object.
					    	System.out.println("hashCode of new Object = " + x.o.hashCode());
					    }
					}
					"""
			},
			options,
			"");
}

// combined test from comment 20 in https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
//  - version with 'this' field references
public void test_nullable_field_11a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
			new String[] {
				"X.java",
				"""
					import org.eclipse.jdt.annotation.*;
					class X {
					    @Nullable Object o;
					    public @NonNull Object foo() {
					    	return  o != null ? o : new Object();
					    }
					    public void goo() {
					    	if (o != null) {
					    		o.toString();
					    	}
					    }
					    public void boo() {
					    	if (o instanceof String) {
					    		o.toString();
					    	}
					    }
					    public void zoo() {
					    	o = new Object();
					    	System.out.println("hashCode of new Object = " + o.hashCode());
					    }
					    public void doo() {
					    	o = foo(); // foo is guaranteed to return @NonNull Object.
					    	System.out.println("hashCode of new Object = " + o.hashCode());
					    }
					}
					"""
			},
			options,
			"");
}

// protected access to nullable field - expiration of information
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_12() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o1, o2, o3, o4;
				    public void foo(X other) {
				         if (other.o1 != null){
								System.out.println(goo()+other.o1.toString()); // warn here: expired by call to goo()
						  }
				         Object x = o2 != null ? o2 : o1;
				         System.out.println(o2.toString()); // warn here: not protected
				         if (o3 != null) /*nop*/;
				         System.out.println(o3.toString()); // warn here: expired by empty statement
				         if (o4 != null && hoo())
				             System.out.println(o4.toString()); // warn here: expired by call to hoo()
				    }
				    String goo() { return ""; }
				    boolean hoo() { return false; }
				}
				"""
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	System.out.println(goo()+other.o1.toString()); // warn here: expired by call to goo()\n" +
		"	                               ^^\n" +
		potNPE_nullable("The field o1") +
		"----------\n" +
		"2. ERROR in X.java (at line 9)\n" +
		"	System.out.println(o2.toString()); // warn here: not protected\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o2") +
		"----------\n" +
		"3. ERROR in X.java (at line 11)\n" +
		"	System.out.println(o3.toString()); // warn here: expired by empty statement\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o3") +
		"----------\n" +
		"4. ERROR in X.java (at line 13)\n" +
		"	System.out.println(o4.toString()); // warn here: expired by call to hoo()\n" +
		"	                   ^^\n" +
		potNPE_nullable("The field o4") +
		"----------\n");
}

// example from comment 47
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_13() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o1;
				    @NonNull Object o2 = new Object();
				    public void foo(X other) {
				         if (other.o1 == null){
								this.o2 = other.o1; // warn here: assign @Nullable to @NonNull
						  }
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	this.o2 = other.o1; // warn here: assign @Nullable to @NonNull\n" +
		"	          ^^^^^^^^\n" +
		mismatch_NonNull_Nullable("Object") +
		"----------\n");
}

// access to a nullable field - protected by check against a @NonNull value
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_14() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o = new Object();
				    public String oString(@NonNull Object a) {
				         if (this.o == a)
				             return this.o.toString();
				         return "";
				    }
				}
				"""
		},
		options,
		"");
}

// access to a nullable field - not protected by negative check against a @NonNull value
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331649
public void test_nullable_field_14a() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o = new Object();
				    public String oString(@NonNull Object a) {
				         if (this.o != a)
				             return this.o.toString(); // warn here, check has no effect
				         return "";
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	return this.o.toString(); // warn here, check has no effect\n" +
		"	            ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}

// https://bugs.eclipse.org/401017: [compiler][null] casted reference to @Nullable field lacks a warning
public void test_nullable_field_15() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable
				    private Object nullable;
				
				    public void test() {
				        if (nullable instanceof Number) {
				            ((Number)nullable).intValue(); // A
				        }
				        if (nullable != null) {
				            nullable.toString(); // B
				        }
				        nullable.toString(); // C
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 8)\n" +
		"	((Number)nullable).intValue(); // A\n" +
		"	         ^^^^^^^^\n" +
		potNPE_nullable("The field nullable") +
		"----------\n" +
		"2. ERROR in X.java (at line 11)\n" +
		"	nullable.toString(); // B\n" +
		"	^^^^^^^^\n" +
		potNPE_nullable("The field nullable") +
		"----------\n" +
		"3. ERROR in X.java (at line 13)\n" +
		"	nullable.toString(); // C\n" +
		"	^^^^^^^^\n" +
		potNPE_nullable("The field nullable") +
		"----------\n");
}
// access to a nullable field - dereference after check in while loop
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=414761
public void test_nullable_field_16() {
	// currently no flow analysis for fields is implemented,
	// but the direct sequence of null-check + dereference is optionally supported as a special case
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object prop;
				    void testWhileAlone(){
				        while(this.prop != null) {
				          test(this.prop);
				        }
				    }
					 @Nullable Object other;
					 void testTwoFields() {
						 boolean b = this.other != null;
				        while(this.prop != null) {
				          test(this.prop);
				        }
				    }
					 void testWhileInIf() {
						 if (this.prop != null) {
				       	 while(this.other != null) {
				       	   test(this.prop);
				       	 }
						 }
					 }
				    void test(@NonNull Object param){
				        assert param != null;
				    }\
				}
				"""
		},
		options /*customOptions*/,
		"----------\n" +
		"1. ERROR in X.java (at line 19)\n" +
		"	test(this.prop);\n" +
		"	     ^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Null type mismatch: required '@NonNull Object' but the provided value is specified as @Nullable\n"
		: "Null type mismatch (type annotations): required \'@NonNull Object\' but this expression has type \'@Nullable Object\'\n") +
		"----------\n");
}
// access to a nullable field - field reference
// Configured as of https://bugs.eclipse.org/bugs/show_bug.cgi?id=433615
public void test_nullable_field_17() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.INFO);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    @Nullable Object o = new Object();
				    public String oString() {
				         return this.o.toString();
				    }
				}
				"""
		},
		options /*customOptions*/,
		"----------\n" +
		"1. INFO in X.java (at line 5)\n" +
		"	return this.o.toString();\n" +
		"	            ^\n" +
		potNPE_nullable("The field o") +
		"----------\n");
}
// an enum is declared within the scope of a null-default
// https://bugs.eclipse.org/331649#c61
public void test_enum_field_01() {
	runConformTestWithLibs(
		new String[] {
			"tests/X.java",
			"""
				package tests;
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class X {
				    enum A { B }
				    public static void main(String ... args) {
				         System.out.println(A.B);
				    }
				}
				"""
		},
		null,
		"",
		"B");
}

// Bug 380896 - Enum constants not recognised as being NonNull.
// see also https://bugs.eclipse.org/331649#c61
public void test_enum_field_02() {
	runConformTestWithLibs(
		new String[] {
			"tests/X.java",
			"""
				package tests;
				import org.eclipse.jdt.annotation.*;
				public class X {
				    enum A { B }
				    public static void main(String ... args) {
				         test(A.B);
				    }
				    static void test(@NonNull A a) {
				        System.out.println(a.ordinal());
				    }
				}
				"""
		},
		null,
		"",
		"0");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372011
// Test whether @NonNullByDefault on a binary package or an enclosing type is respected from enclosed elements.
public void testBug372011() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test372011.jar";
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	runNegativeNullTest(
		new String[] {
			"X.java",
			  """
				import p11.T11;
				import p12.T12;
				import p12.T12a;
				import p12.Public;
				public class X {
					  void foo() {
				     new T11().t11foo(null);
				     new T12().new T122().foo122(null);
				   }
					  void trigger1 (Public o){
							o.bar(null);
					  }
					  @org.eclipse.jdt.annotation.NonNull Object foo2() {
						new T12a().foo12a(new Object());
				     new T12().new T122().new T1222().foo1222(null);
				     return new T11().retSomething();
				   }
				}
				"""},
	    """
			----------
			1. ERROR in X.java (at line 7)
				new T11().t11foo(null);
				                 ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in X.java (at line 8)
				new T12().new T122().foo122(null);
				                            ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			3. ERROR in X.java (at line 11)
				o.bar(null);
				      ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			4. ERROR in X.java (at line 15)
				new T12().new T122().new T1222().foo1222(null);
				                                         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""",
		libs,
		true /* shouldFlush*/,
		getCompilerOptions());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=374129  - more tests for bug 372011
// Test whether @NonNullByDefault on a binary package or an enclosing type is respected from enclosed elements.
public void testBug374129() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test374129"+this.TEST_JAR_SUFFIX;
	/* content of Test372129.jar:
	 	p1bin/package-info.java:
	 		@org.eclipse.jdt.annotation.NonNullByDefault
			package p1bin;
		p1bin/C1bin.java:
			package p1bin;
			import org.eclipse.jdt.annotation.Nullable;
			public class C1bin {
				public String getId(String id, @Nullable String n) {
					return id;
				}
				public static class C1binInner {
					public String getId(String id, @Nullable String n) {
						return id;
					}
				}
			}
		p2bin/C2bin.java:
			package p2bin;
			import org.eclipse.jdt.annotation.NonNullByDefault;
			import org.eclipse.jdt.annotation.Nullable;
			@NonNullByDefault
			public class C2bin {
				public String getId(String id, @Nullable String n) {
					return id;
				}
				@NonNullByDefault(false)
				public static class C2binInner {
					public String getId(String id, @Nullable String n) {
						return id;
					}
				}
			}
		p2bin/C3bin.java:
			package p2bin;
			import org.eclipse.jdt.annotation.NonNullByDefault;
			import org.eclipse.jdt.annotation.Nullable;
			public class C3bin {
				@NonNullByDefault public String getId(String id, @Nullable String n) {
					return id;
				}
			}
	 */
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	runNegativeNullTest(
		new String[] {
			"bug374129/Test.java",
				"""
					package bug374129;
					
					import org.eclipse.jdt.annotation.NonNull;
					import org.eclipse.jdt.annotation.Nullable;
					
					import p1bin.C1bin;
					import p1bin.C1bin.C1binInner;
					import p2bin.C2bin;
					import p2bin.C2bin.C2binInner;
					import p2bin.C3bin;
					
					public class Test {
						static C1bin c1 = new C1bin();
						static C1binInner c1i = new C1binInner();
						static C2bin c2 = new C2bin();
						static C2binInner c2i = new C2binInner();
						static C3bin c3 = new C3bin();
					\t
						public static void main(String[] args) {
							@Nullable String n = getN();
							@NonNull String s;
							s = c1.getId(n, n); // error on first arg (package default)
							s = c1i.getId(n, n); // error on first arg (package default propagated into inner)
							s = c2.getId(n, n); // error on first arg (type default)
							s = c2i.getId(n, n); // no arg error (canceled default), return requires unchecked conversion
							s = c3.getId(n, n); // error on first arg (method default)
						}
						static String getN() { return null; }
					}
					
					"""},
			"----------\n" +
			"1. ERROR in bug374129\\Test.java (at line 22)\n" +
			"	s = c1.getId(n, n); // error on first arg (package default)\n" +
			"	             ^\n" +
			mismatch_NonNull_Nullable("String") +
			"----------\n" +
			"2. ERROR in bug374129\\Test.java (at line 23)\n" +
			"	s = c1i.getId(n, n); // error on first arg (package default propagated into inner)\n" +
			"	              ^\n" +
			mismatch_NonNull_Nullable("String") +
			"----------\n" +
			"3. ERROR in bug374129\\Test.java (at line 24)\n" +
			"	s = c2.getId(n, n); // error on first arg (type default)\n" +
			"	             ^\n" +
			mismatch_NonNull_Nullable("String") +
			"----------\n" +
			"4. WARNING in bug374129\\Test.java (at line 25)\n" +
			"	s = c2i.getId(n, n); // no arg error (canceled default), return requires unchecked conversion\n" +
			"	    ^^^^^^^^^^^^^^^\n" +
			nullTypeSafety() + "The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" +
			"----------\n" +
			"5. ERROR in bug374129\\Test.java (at line 26)\n" +
			"	s = c3.getId(n, n); // error on first arg (method default)\n" +
			"	             ^\n" +
			mismatch_NonNull_Nullable("String") +
			"----------\n",
		libs,
		true /* shouldFlush*/,
		getCompilerOptions());
}

// Bug 385626 - @NonNull fails across loop boundaries
public void testBug385626_1() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void test() {
				        for (Integer i : new ArrayList<Integer>()) {
				            if (i != null) {
				                for (Integer j : new ArrayList<Integer>()) {
				                    if (j != null) {
				                        @NonNull Integer j1 = i; // bogus error was here
				                    }
				                }
				            }
				        }
				    }
				}
				"""
		},
		null,//options
		"");
}

// Bug 385626 - @NonNull fails across loop boundaries
public void testBug385626_2() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import org.eclipse.jdt.annotation.*;
				public class X {
				    void test(Integer j) {
				        for (Integer i : new ArrayList<Integer>()) {
				            if (i != null) {
				                try {
				                    if (j != null) {
				                        @NonNull Integer j1 = i;
				                    }
				                } finally {
				                    if (j != null) {
				                        @NonNull Integer j1 = i; // bogus error was here
				                    }
				                }
				            }
				        }
				    }
				}
				"""
		},
		null,//options
		"");
}

// Bug 388630 - @NonNull diagnostics at line 0
// synthetic constructor must repeat null annotations of its super
public void testBug388630_1() {
	runConformTestWithLibs(
		new String[] {
			"C0.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				public class C0 {
					C0 (@NonNull Object o) { }
					void test() { }
				}
				""",
			"X.java",
			"""
				public class X {
					void foo() {
						new C0("") { }.test();
					}
				}
				"""
		},
		null,
		"");
}

// Bug 388630 - @NonNull diagnostics at line 0
// additionally also references to outer variables must share their nullness
public void testBug388630_2() {
	runNegativeTestWithLibs(
		new String[] {
			"C0.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				public class C0 {
					C0 (@NonNull Object o) { }
					void test() { }
				}
				""",
			"X.java",
			"""
				import org.eclipse.jdt.annotation.Nullable;
				public class X {
					void foo(final @Nullable Object a) {
						new C0("") {
				           @Override
				           void test() {
				               System.out.println(a.toString());
				               super.test();
				           }
				       }.test();
					}
				}
				"""
		},
		null,
		"----------\n" +
		"1. ERROR in X.java (at line 7)\n" +
		"	System.out.println(a.toString());\n" +
		"	                   ^\n" +
		variableMayBeNull("a") +
		"----------\n");
}

/* Content of Test388281.jar used in the following tests:

// === package i (explicit annotations): ===
package i;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
public interface I {
    @NonNull Object m1(@Nullable Object a1);
    @Nullable String m2(@NonNull Object a2);
	Object m1(@Nullable Object o1, Object o2);
}

// === package  i2 with package-info.java (default annot, canceled in one type): ===
@org.eclipse.jdt.annotation.NonNullByDefault
package i2;

package i2;
public interface I2 {
    Object m1(Object a1);
    String m2(Object a2);
}

package i2;
public interface II extends i.I {
	String m1(Object o1, Object o2);
}

package i2;
import org.eclipse.jdt.annotation.NonNullByDefault;
@NonNullByDefault({})
public interface I2A {
    Object m1(Object a1);
    String m2(Object a2);
}

// === package c (no null annotations): ===
package c;
public class C1 implements i.I {
	public Object m1(Object a1) {
		System.out.println(a1.toString()); // (1)
		return null; // (2)
	}
	public String m2(Object a2) {
		System.out.println(a2.toString());
		return null;
	}
	public Object m1(Object o1, Object o2) {
		return null;
	}
}

package c;
public class C2 implements i2.I2 {
	public Object m1(Object a1) {
		return a1;
	}
	public String m2(Object a2) {
		return a2.toString();
	}
}
 */
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Test whether null annotations from a super interface are respected
// Class and its super interface both read from binary
public void testBug388281_01() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281"+this.TEST_JAR_SUFFIX;
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeNullTest(
		new String[] {
			"Client.java",
			"""
				import c.C1;
				public class Client {
				    void test(C1 c) {
				         String s = c.m2(null);               // (3)
				         System.out.println(s.toUpperCase()); // (4)
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Client.java (at line 4)
				String s = c.m2(null);               // (3)
				                ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in Client.java (at line 5)
				System.out.println(s.toUpperCase()); // (4)
				                   ^
			Potential null pointer access: The variable s may be null at this location
			----------
			""",
		libs,
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Test whether null annotations from a super interface are respected
// Class from source, its supers (class + super interface) from binary
public void testBug388281_02() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281"+this.TEST_JAR_SUFFIX;
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeNullTest(
		new String[] {
			"ctest/C.java",
			"""
				package ctest;
				public class C extends c.C1 {
				    @Override
				    public Object m1(Object a1) {
				         System.out.println(a1.toString());   // (1)
				         return null;                         // (2)
				    }
				    @Override
				    public String m2(Object a2) {
				         System.out.println(a2.toString());
				         return null;
				    }
				}
				""",
			"Client.java",
			"""
				import ctest.C;
				public class Client {
				    void test(C c) {
				         String s = c.m2(null);               // (3)
				         System.out.println(s.toUpperCase()); // (4)
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in ctest\\C.java (at line 5)\n" +
		"	System.out.println(a1.toString());   // (1)\n" +
		"	                   ^^\n" +
		potNPE_nullable_maybenull("The variable a1") +
		"----------\n" +
		"2. ERROR in ctest\\C.java (at line 6)\n" +
		"	return null;                         // (2)\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Client.java (at line 4)\n" +
		"	String s = c.m2(null);               // (3)\n" +
		"	                ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"2. ERROR in Client.java (at line 5)\n" +
		"	System.out.println(s.toUpperCase()); // (4)\n" +
		"	                   ^\n" +
		"Potential null pointer access: The variable s may be null at this location\n" +
		"----------\n",
		libs,
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Test whether null annotations from a super interface trigger an error against the overriding implementation
// Class from source, its super interface from binary
public void testBug388281_03() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281"+this.TEST_JAR_SUFFIX;
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeNullTest(
		new String[] {
			"ctest/C.java",
			"""
				package ctest;
				public class C implements i.I {
				    public Object m1(Object a1) {
				         System.out.println(a1.toString());   // (1)
				         return null;                         // (2)
				    }
				    public String m2(Object a2) {
				         System.out.println(a2.toString());
				         return null;
				    }
				    public Object m1(Object a1, Object a2) {
				        System.out.println(a1.toString());   // (3)
				        return null;
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in ctest\\C.java (at line 4)\n" +
		"	System.out.println(a1.toString());   // (1)\n" +
		"	                   ^^\n" +
		potNPE_nullable_maybenull("The variable a1") +
		"----------\n" +
		"2. ERROR in ctest\\C.java (at line 5)\n" +
		"	return null;                         // (2)\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"3. ERROR in ctest\\C.java (at line 12)\n" +
		"	System.out.println(a1.toString());   // (3)\n" +
		"	                   ^^\n" +
		potNPE_nullable_maybenull("The variable a1") +
		"----------\n",
		libs,
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Do inherit even if one parameter/return is annotated
// also features some basic overloading
public void testBug388281_04() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		true /* shouldFlush*/,
		new String[] {
			"i/I.java",
			"""
				package i;
				import org.eclipse.jdt.annotation.*;
				public interface I {
				    @NonNull Object m1(@NonNull Object s1, @Nullable String s2);
				    @Nullable Object m1(@Nullable String s1, @NonNull Object s2);
				}
				""",
			"ctest/C.java",
			"""
				package ctest;
				import org.eclipse.jdt.annotation.*;
				public class C implements i.I {
				    public Object m1(@Nullable Object o1, String s2) {
				         System.out.println(s2.toString());   // (1)
				         return null;                         // (2)
				    }
				    public @NonNull Object m1(String s1, Object o2) {
				         System.out.println(s1.toString());   // (3)
				         return new Object();
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. ERROR in ctest\\C.java (at line 5)\n" +
		"	System.out.println(s2.toString());   // (1)\n" +
		"	                   ^^\n" +
		variableMayBeNull("s2") +
		"----------\n" +
		"2. ERROR in ctest\\C.java (at line 6)\n" +
		"	return null;                         // (2)\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n" +
		"3. ERROR in ctest\\C.java (at line 9)\n" +
		"	System.out.println(s1.toString());   // (3)\n" +
		"	                   ^^\n" +
		variableMayBeNull("s1") +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Test whether null annotations from a super interface trigger an error against the overriding implementation
// Class from source, its super interface from binary
// Super interface subject to package level @NonNullByDefault
public void testBug388281_05() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281"+this.TEST_JAR_SUFFIX;
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeNullTest(
		new String[] {
			"ctest/C.java",
			"""
				package ctest;
				public class C implements i2.I2 {
				    public Object m1(Object a1) {
				         System.out.println(a1.toString());   // silent
				         return null;                         // (1)
				    }
				    public String m2(Object a2) {
				         System.out.println(a2.toString());
				         return null;						   // (2)
				    }
				}
				""",
			"Client.java",
			"""
				import ctest.C;
				public class Client {
				    void test(C c) {
				         String s = c.m2(null);               // (3)
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in ctest\\C.java (at line 5)
				return null;                         // (1)
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			2. ERROR in ctest\\C.java (at line 9)
				return null;						   // (2)
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			----------
			1. ERROR in Client.java (at line 4)
				String s = c.m2(null);               // (3)
				                ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""",
		libs,
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// Conflicting annotations from several indirect super interfaces must be detected
public void testBug388281_06() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281"+this.TEST_JAR_SUFFIX;
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeNullTest(
		new String[] {
			"ctest/C.java",
			"""
				package ctest;
				public class C extends c.C2 implements i2.I2A {
				}
				"""
		},
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"""
			----------
			1. ERROR in ctest\\C.java (at line 2)
				public class C extends c.C2 implements i2.I2A {
				                       ^^^^
			The method m2(Object) from C2 cannot implement the corresponding method from I2A due to incompatible nullness constraints
			----------
			2. ERROR in ctest\\C.java (at line 2)
				public class C extends c.C2 implements i2.I2A {
				                       ^^^^
			The method m1(Object) from C2 cannot implement the corresponding method from I2A due to incompatible nullness constraints
			----------
			"""
		: """
			----------
			1. ERROR in ctest\\C.java (at line 2)
				public class C extends c.C2 implements i2.I2A {
				                       ^^^^
			The method m2(@NonNull Object) from C2 cannot implement the corresponding method from I2A due to incompatible nullness constraints
			----------
			2. ERROR in ctest\\C.java (at line 2)
				public class C extends c.C2 implements i2.I2A {
				                       ^^^^
			The method m1(@NonNull Object) from C2 cannot implement the corresponding method from I2A due to incompatible nullness constraints
			----------
			"""),
		libs,
		true /* shouldFlush*/,
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// report conflict between inheritance and default
public void testBug388281_07() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"p1/Super.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public class Super {
				    public @Nullable Object m(@Nullable Object arg) {
				        return null;\
				    }
				}
				""",
			"p2/Sub.java",
			"""
				package p2;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Sub extends p1.Super {
				    @Override
				    public Object m(Object arg) { // (a)+(b) conflict at arg and return
				        System.out.println(arg.toString()); // (1)
				        return null;
				    }
				}
				""",
			"Client.java",
			"""
				public class Client {
				    void test(p2.Sub s) {
				        Object result = s.m(null);
				        System.out.println(result.toString());  // (2)
				    }
				}
				"""
		},
		options,
		"----------\n" +
		"1. ERROR in p2\\Sub.java (at line 6)\n" +
		"	public Object m(Object arg) { // (a)+(b) conflict at arg and return\n" +
		"	       ^^^^^^\n" +
		"The default \'@NonNull\' conflicts with the inherited \'@Nullable\' annotation in the overridden method from Super\n" +
		"----------\n" +
		"2. ERROR in p2\\Sub.java (at line 6)\n" +
		"	public Object m(Object arg) { // (a)+(b) conflict at arg and return\n" +
		"	                       ^^^\n" +
		"The default \'@NonNull\' conflicts with the inherited \'@Nullable\' annotation in the overridden method from Super\n" +
		"----------\n" +
		"3. ERROR in p2\\Sub.java (at line 7)\n" +
		"	System.out.println(arg.toString()); // (1)\n" +
		"	                   ^^^\n" +
		variableMayBeNull("arg") +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Client.java (at line 4)\n" +
		"	System.out.println(result.toString());  // (2)\n" +
		"	                   ^^^^^^\n" +
		"Potential null pointer access: The variable result may be null at this location\n" +
		"----------\n");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// report conflict between inheritance and default - binary types
public void testBug388281_08() {
	String path = this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test388281"+this.TEST_JAR_SUFFIX;
	String[] libs = new String[this.LIBS.length + 1];
	System.arraycopy(this.LIBS, 0, libs, 0, this.LIBS.length);
	libs[this.LIBS.length] = path;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeNullTest(
		new String[] {
			"ctest/Ctest.java",
			"""
				package ctest;
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public class Ctest implements i2.II {
				    public Object m1(@Nullable Object a1) { // silent: conflict at a1 avoided
						return new Object();
				    }
				    public String m2(Object a2) { // (a) conflict at return
				    	return null;
				    }
				    public String m1(Object o1, Object o2) { // (b) conflict at o1
				        System.out.println(o1.toString()); // (1) inherited @Nullable
				        return null; // (2) @NonNullByDefault in i2.II
				    }
				}
				""",
			"Client.java",
			"""
				public class Client {
				    void test(ctest.Ctest c) {
				        Object result = c.m1(null, null); // (3) 2nd arg @NonNullByDefault from i2.II
				    }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in ctest\\Ctest.java (at line 8)\n" +
		"	public String m2(Object a2) { // (a) conflict at return\n" +
		"	       ^^^^^^\n" +
		"The default \'@NonNull\' conflicts with the inherited \'@Nullable\' annotation in the overridden method from I\n" +
		"----------\n" +
		"2. ERROR in ctest\\Ctest.java (at line 11)\n" +
		"	public String m1(Object o1, Object o2) { // (b) conflict at o1\n" +
		"	                        ^^\n" +
		"The default \'@NonNull\' conflicts with the inherited \'@Nullable\' annotation in the overridden method from II\n" +
		"----------\n" +
		"3. ERROR in ctest\\Ctest.java (at line 12)\n" +
		"	System.out.println(o1.toString()); // (1) inherited @Nullable\n" +
		"	                   ^^\n" +
		potNPE_nullable_maybenull("The variable o1") +
		"----------\n" +
		"4. ERROR in ctest\\Ctest.java (at line 13)\n" +
		"	return null; // (2) @NonNullByDefault in i2.II\n" +
		"	       ^^^^\n" +
		"Null type mismatch: required \'@NonNull String\' but the provided value is null\n" +
		"----------\n" +
		"----------\n" +
		"1. ERROR in Client.java (at line 3)\n" +
		"	Object result = c.m1(null, null); // (3) 2nd arg @NonNullByDefault from i2.II\n" +
		"	                           ^^^^\n" +
		"Null type mismatch: required \'@NonNull Object\' but the provided value is null\n" +
		"----------\n",
		libs,
		true, // should flush
		options);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// difference between inherited abstract & non-abstract methods
public void testBug388281_09() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"p1/Super.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public abstract class Super {
				    public abstract @NonNull Object compatible(@Nullable Object arg);
				    public @Nullable Object incompatible(int dummy, @NonNull Object arg) {
				        return null;\
				    }
				}
				""",
			"p1/I.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public interface I {
				    public @Nullable Object compatible(@NonNull Object arg);
				    public @NonNull Object incompatible(int dummy, @Nullable Object arg);
				}
				""",
			"p2/Sub.java",
			"""
				package p2;
				public class Sub extends p1.Super implements p1.I {
				    @Override
				    public Object compatible(Object arg) {
				        return this;
				    }
				    @Override
				    public Object incompatible(int dummy, Object arg) {
				        return null;
				    }
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in p2\\Sub.java (at line 4)
				public Object compatible(Object arg) {
				       ^^^^^^
			Conflict between inherited null annotations \'@Nullable\' declared in I versus \'@NonNull\' declared in Super\s
			----------
			2. ERROR in p2\\Sub.java (at line 4)
				public Object compatible(Object arg) {
				                         ^^^^^^
			Conflict between inherited null annotations \'@NonNull\' declared in I versus \'@Nullable\' declared in Super\s
			----------
			3. ERROR in p2\\Sub.java (at line 8)
				public Object incompatible(int dummy, Object arg) {
				       ^^^^^^
			Conflict between inherited null annotations \'@NonNull\' declared in I versus \'@Nullable\' declared in Super\s
			----------
			4. ERROR in p2\\Sub.java (at line 8)
				public Object incompatible(int dummy, Object arg) {
				                                      ^^^^^^
			Conflict between inherited null annotations \'@Nullable\' declared in I versus \'@NonNull\' declared in Super\s
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388281
// respect inherited @NonNull also inside the method body, see comment 28
public void testBug388281_10() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
	runNegativeTestWithLibs(
		new String[] {
			"p1/Super.java",
			"""
				package p1;
				import org.eclipse.jdt.annotation.*;
				public class Super {
				    public void m(@NonNull Object arg) {}
				}
				""",
			"p2/Sub.java",
			"""
				package p2;
				public class Sub extends p1.Super  {
				    @Override
				    public void m(Object arg) {
				        arg = null;
				    }
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in p2\\Sub.java (at line 5)
				arg = null;
				      ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertNull vs. a @NonNull field / expression
public void testBug382069_j() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			NullReferenceTestAsserts.JUNIT_ASSERT_NAME,
			NullReferenceTestAsserts.JUNIT_ASSERT_CONTENT,
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				public class X {
				  @NonNull String o1 = "";
				  boolean foo() {
				    junit.framework.Assert.assertNull("something's wrong", o1);
				    return false; // dead code
				  }
				  void bar() {
				      junit.framework.Assert.assertNull("");
				      return; // dead code
				  }
				  void zork() {
				      junit.framework.Assert.assertNotNull(null);
				      return; // dead code
				  }
				}
				"""},
			options,
			"""
				----------
				1. ERROR in X.java (at line 6)
					return false; // dead code
					^^^^^^^^^^^^^
				Dead code
				----------
				2. ERROR in X.java (at line 10)
					return; // dead code
					^^^^^^^
				Dead code
				----------
				3. ERROR in X.java (at line 14)
					return; // dead code
					^^^^^^^
				Dead code
				----------
				""");
}

// https://bugs.eclipse.org/382069 - [null] Make the null analysis consider JUnit's assertNotNull similarly to assertions
// junit's assertNonNull et al. affecting a @Nullable field using syntactic analysis
public void testBug382069_k() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	options.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.ERROR);
	runNegativeTestWithLibs(
		new String[] {
			NullReferenceTestAsserts.JUNIT_ASSERT_NAME,
			NullReferenceTestAsserts.JUNIT_ASSERT_CONTENT,
			"X.java",
			"""
				import org.eclipse.jdt.annotation.Nullable;
				public class X {
				  @Nullable String o1;
				  int foo() {
				    junit.framework.Assert.assertNotNull("something's wrong", o1);
				    return o1.length();
				  }
				  int bar(int i) {
				    junit.framework.Assert.assertNotNull(o1);
				    i++;
				    return o1.length(); // no longer protected
				  }
				  int garp() {
				    junit.framework.Assert.assertFalse("something's wrong", o1 == null);
				    return o1.length();
				  }
				  int zipp() {
				    junit.framework.Assert.assertTrue("something's wrong", o1 != null);
				    return o1.length();
				  }
				}
				"""},
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 11)\n" +
			"	return o1.length(); // no longer protected\n" +
			"	       ^^\n" +
			potNPE_nullable("The field o1") +
			"----------\n");
}
//https://bugs.eclipse.org/400761: [compiler][null] null may be return as boolean without a diagnostic
public void test_conditional_expression_1() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					boolean badFunction5(int i) {
						// expected a potential null problem:
						return i > 0 ? true : getBoolean();
					}
					private @Nullable Boolean getBoolean() {
						return null;
					}
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 5)
				return i > 0 ? true : getBoolean();
				                      ^^^^^^^^^^^^
			Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing
			----------
			""");
}

// Bug 403086 - [compiler][null] include the effect of 'assert' in syntactic null analysis for fields
public void testBug403086_1() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);
	customOptions.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			NullReferenceTestAsserts.JUNIT_ASSERT_NAME,
			NullReferenceTestAsserts.JUNIT_ASSERT_CONTENT,
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Y {
					@Nullable String str;
					int foo(@Nullable String str2) {
						int i;
						junit.framework.Assert.assertNotNull(str);
						i = str.length();
				
						assert this.str != null;
						i = str.length();
				
						return i;
					}
				}
				"""
		},
		customOptions,
		"");
}

//Bug 403086 - [compiler][null] include the effect of 'assert' in syntactic null analysis for fields
public void testBug403086_2() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	customOptions.put(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);
	customOptions.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	runConformTestWithLibs(
		new String[] {
			NullReferenceTestAsserts.JUNIT_ASSERT_NAME,
			NullReferenceTestAsserts.JUNIT_ASSERT_CONTENT,
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				class Y {
					@Nullable String str;
					int foo(@Nullable String str2) {
						int i;
						junit.framework.Assert.assertNotNull(str);
						i = str.length();
				
						assert ! (this.str == null);
						i = str.length();
				
						return i;
					}
				}
				"""
		},
		customOptions,
		"");
}

// https://bugs.eclipse.org/412076 - [compiler] @NonNullByDefault doesn't work for varargs parameter when in generic interface
public void testBug412076() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"Foo.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public interface Foo<V> {
				  V bar(String... values);
				  V foo(String value);
				}
				"""
		},
		options,
		"");
	runConformTestWithLibs(
		false /*flush*/,
		new String[] {
			"FooImpl.java",
			"import org.eclipse.jdt.annotation.*;\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_8
			? "@NonNullByDefault\n"
			: "@NonNullByDefault({DefaultLocation.PARAMETER,DefaultLocation.RETURN_TYPE})\n" // avoid @NonNull on type argument <String>
			) +
			"public class FooImpl implements Foo<String> {\n" +
			"  public String bar(final String... values) {\n" +
			"    return (\"\");\n" +
			"  }\n" +
			"  public String foo(final String value) {\n" +
			"    return (\"\");\n" +
			"  }\n" +
			"}\n"
		},
		options,
		"");
}

public void testBug413460() {
	runConformTestWithLibs(
		new String[] {
			"Class2.java",
			"""
				
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class Class2 {
					public class Class3 {
						public Class3(String nonNullArg) {
							assert nonNullArg != null;
						}
					}
				
					public Class2(String nonNullArg) {
						assert nonNullArg != null;
					}
				
					public static Class2 create(String nonNullArg) {
						return new Class2(nonNullArg);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
	runNegativeTestWithLibs(false,
		new String[] {
			"Class1.java",
			"""
				public class Class1 {
					public static Class2 works() {
						return Class2.create(null);
					}
				
					public static Class2 bug() {
						return new Class2(null);
					}
				
					public static Class2.Class3 qualifiedbug() {
						return new Class2("").new Class3(null);
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Class1.java (at line 3)
				return Class2.create(null);
				                     ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			2. ERROR in Class1.java (at line 7)
				return new Class2(null);
				                  ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			3. ERROR in Class1.java (at line 11)
				return new Class2("").new Class3(null);
				                                 ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			""");
}

// missing type in constructor declaration must not cause NPE in QAE#resolveType(..)
public void testBug415850_a() {
	runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1(x1){};
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1(x1){};
					               ^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					public X1(Zork z) {}
					          ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			this.LIBS,
			true/*flush*/,
			null/*options*/);
}

// avoid NPE in BinaryTypeBinding.getField(..) due to recursive dependency enum->package-info->annotation->enum
public void testBug415850_b() {
	runConformTestWithLibs(
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
			"test180/Test.java",
			"""
				package test180;
				import p.MyEnum;
				import p.Annot;
				@Annot(state=MyEnum.OK)
				public class Test {}""",
		},
		getCompilerOptions(),
		""
	);
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	runConformTestWithLibs(
		false /* don't flush output dir */,
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
		options,
		"",
		"class test180.Test");
}
public void testBug417295_5() {
	runNegativeTestWithLibs(
		new String[] {
			"AllAreNonNull.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class AllAreNonNull {
					String s3 = "";
					void test() {
						this.s3 = null;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in AllAreNonNull.java (at line 5)
				this.s3 = null;
				          ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			""");
}
public void testBug417295_7() {
	runConformTestWithLibs(
			new String[] {
				"p1/AllAreNonNull.java",
				"""
					package p1;
					@org.eclipse.jdt.annotation.NonNullByDefault
					public class AllAreNonNull {
						public String s3 = "";
					}
					"""
			},
			getCompilerOptions(),
			"");
	runNegativeTestWithLibs(
		false,
		new String[] {
			"Client.java",
			"""
				public class Client {
					void test(p1.AllAreNonNull aann) {
						aann.s3 = null;
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in Client.java (at line 3)
				aann.s3 = null;
				          ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			""");
}
// Bug 415413 - [compiler][null] NullpointerException in Null Analysis caused by interaction of LoopingFlowContext and FinallyFlowContext
public void testBug415413() {
	Map options = getCompilerOptions();
	runNegativeTestWithLibs(
		new String[]{
			"ClassF.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				public class ClassF {
				  public static void needNonNull(@NonNull Object o) {
				    o.hashCode();
				  }
				  public void method() {
				    for (int j = 0; j < 1; j++) {
				      try {
				        this.hashCode();
				      } finally {
				        for (int i = 0; i < 1; i++) {
				          Object o = null;
				          needNonNull(o);
				        }
				      }
				    }
				  }
				}
				"""
		},
		options,
		"""
			----------
			1. ERROR in ClassF.java (at line 13)
				needNonNull(o);
				            ^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			""");
}
// Bug 415413 - [compiler][null] NullpointerException in Null Analysis caused by interaction of LoopingFlowContext and FinallyFlowContext
// Variant: non-null before the loop and at the end of the loop body
public void testBug415413a() {
 Map options = getCompilerOptions();
 runConformTestWithLibs(
     new String[]{
         "ClassF.java",
         """
			import org.eclipse.jdt.annotation.NonNull;
			public class ClassF {
			  public static void needNonNull(@NonNull Object o) {
			    o.hashCode();
			  }
			  public void method() {
			    for (int j = 0; j < 1; j++) {
			      try {
			        this.hashCode();
			      } finally {
			        Object o = new Object();
			        for (int i = 0; i < 1; i++) {
			          needNonNull(o);
			          o = new Object();
			        }
			      }
			    }
			  }
			}
			"""
     },
     options,
     "");
}
// Bug 415413 - [compiler][null] NullpointerException in Null Analysis caused by interaction of LoopingFlowContext and FinallyFlowContext
// Variant: null before the loop and non-null at the end of the loop body
public void testBug415413b() {
 Map options = getCompilerOptions();
 runNegativeTestWithLibs(
     new String[]{
         "ClassF.java",
         """
			import org.eclipse.jdt.annotation.NonNull;
			public class ClassF {
			  public static void needNonNull(@NonNull Object o) {
			    o.hashCode();
			  }
			  public void method() {
			    for (int j = 0; j < 1; j++) {
			      try {
			        this.hashCode();
			      } finally {
			        Object o = null;
			        for (int i = 0; i < 1; i++) {
			          needNonNull(o);
			          o = new Object();
			        }
			      }
			    }
			  }
			}
			"""
     },
     options,
     """
		----------
		1. ERROR in ClassF.java (at line 13)
			needNonNull(o);
			            ^
		Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable
		----------
		""");
}
// Bug 415413 - [compiler][null] NullpointerException in Null Analysis caused by interaction of LoopingFlowContext and FinallyFlowContext
// Variant: non-null before the loop and null at the end of the loop body
public void testBug415413c() {
 Map options = getCompilerOptions();
 runNegativeTestWithLibs(
     new String[]{
         "ClassF.java",
         """
			import org.eclipse.jdt.annotation.NonNull;
			public class ClassF {
			  public static void needNonNull(@NonNull Object o) {
			    o.hashCode();
			  }
			  public void method() {
			    for (int j = 0; j < 1; j++) {
			      try {
			        this.hashCode();
			      } finally {
			        Object o = new Object();
			        for (int i = 0; i < 1; i++) {
			          needNonNull(o);
			          o = null;
			        }
			      }
			    }
			  }
			}
			"""
     },
     options,
     """
		----------
		1. ERROR in ClassF.java (at line 13)
			needNonNull(o);
			            ^
		Null type mismatch: required \'@NonNull Object\' but the provided value is inferred as @Nullable
		----------
		""");
}
public void testBug_415269() {
	Map options = getCompilerOptions();
	runConformTestWithLibs(
		new String[]{
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				public class Y {
				  public static class C implements X.I {
				    public void method(@NonNull Object arg) {
				    }
				  }
				}
				""",
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				@NonNullByDefault
				public class X {
				  public interface I {
				    public void method(Object arg);
				  }
				}
				"""
		},
		options,
		"");
}
public void testBug416267() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void test() {
						Missing m = new Missing() { };
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Missing m = new Missing() { };
				^^^^^^^
			Missing cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				Missing m = new Missing() { };
				                ^^^^^^^
			Missing cannot be resolved to a type
			----------
			""",
		this.LIBS,
		true, /*flush*/
		null /*options*/);
}
//duplicate of bug 416267
public void testBug418843() {
	runNegativeTest(
		new String[] {
			"TestEnum.java",
			"""
				public enum TestEnum {
					TestEntry(1){};
				}"""
		},
		"""
			----------
			1. ERROR in TestEnum.java (at line 2)
				TestEntry(1){};
				^^^^^^^^^
			The constructor TestEnum(int) is undefined
			----------
			""",
		this.LIBS,
		true,/*flush*/
		null/*options*/);
}
public void testBug418235() {
	String[] testFiles =
            new String[] {
                    "GenericInterface.java",
                    """
						public interface GenericInterface<T> {
						       T doSomethingGeneric(T o);
						}""",
                    "Implementation.java",
                    "import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
                    "@NonNullByDefault\n" +
                    "public class Implementation implements GenericInterface<Object> {\n" +
                    "\n" +
                    (this.complianceLevel < ClassFileConstants.JDK1_6 ? "\n" : "      @Override\n" ) +
                    "       public Object doSomethingGeneric(Object o) {\n" +
                    "               return o;\n" +
                    "       }\n" +
                    "}\n"
			};
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
	    runNegativeTestWithLibs(
	            testFiles,
	            """
					----------
					1. ERROR in Implementation.java (at line 6)
						public Object doSomethingGeneric(Object o) {
						                                 ^^^^^^
					Illegal redefinition of parameter o, inherited method from GenericInterface<Object> does not constrain this parameter
					----------
					""");
	} else {
		// in 1.8 the nullness default also affects the type argument <Object> from which T is instantiated to '@NonNull Object'
		runConformTestWithLibs(
				testFiles, getCompilerOptions(), "");
	}
}
public void testBug418235b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	runNegativeTestWithLibs(
	        new String[] {
			    "GenericInterface.java",
			    """
					public interface GenericInterface<T> {
					       T doSomethingGeneric(T o);
					}""",
			    "Implementation.java",
			    """
					import org.eclipse.jdt.annotation.*;
					@NonNullByDefault({DefaultLocation.PARAMETER,DefaultLocation.RETURN_TYPE})
					public class Implementation implements GenericInterface<Object> {
					
					      @Override
					       public Object doSomethingGeneric(Object o) {
					               return o;
					       }
					}
					"""
			},
	        """
				----------
				1. ERROR in Implementation.java (at line 6)
					public Object doSomethingGeneric(Object o) {
					                                 ^^^^^^
				Illegal redefinition of parameter o, inherited method from GenericInterface<Object> does not constrain this parameter
				----------
				""");
}

public void testTypeAnnotationProblemNotIn17() {
	String source =
			"""
		import org.eclipse.jdt.annotation.*;
		public class X {
			public @NonNull java.lang.String test(@NonNull java.lang.String arg) {
				@NonNull java.lang.String local = arg;
				return local;
			}
		}
		""";
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		runConformTestWithLibs(
			new String[] {
				"X.java",
				source
			},
			getCompilerOptions(),
			"");
	else
		runNegativeTest(
			new String[] {
				"X.java",
				source
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					public @NonNull java.lang.String test(@NonNull java.lang.String arg) {
					       ^^^^^^^^
				Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
				----------
				2. ERROR in X.java (at line 3)
					public @NonNull java.lang.String test(@NonNull java.lang.String arg) {
					                                      ^^^^^^^^
				Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
				----------
				3. ERROR in X.java (at line 4)
					@NonNull java.lang.String local = arg;
					^^^^^^^^
				Illegally placed annotation: type annotations must directly precede the simple name of the type they are meant to affect (or the [] for arrays)
				----------
				""",
			this.LIBS,
			true, // flush
			getCompilerOptions());
}
public void testBug420313() {
	runWarningTestWithLibs(
		true, /*flush*/
		new String[] {
			"OverrideTest.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				
				public class OverrideTest implements TypedBase<String>, UntypedBase
				{
				   public void doSomething(String text) // No warning
				   {
				      System.out.println(text);
				   }
				  \s
				   public void doSomethingElse(String text) // "Missing non-null annotation" warning
				   {
				      System.out.println(text);
				   }
				}
				
				interface TypedBase<T>
				{
				   void doSomething(@NonNull T text);
				}
				
				interface UntypedBase
				{
				   void doSomethingElse(@NonNull String text);
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. WARNING in OverrideTest.java (at line 5)
				public void doSomething(String text) // No warning
				                        ^^^^^^
			Missing non-null annotation: inherited method from TypedBase<String> specifies this parameter as @NonNull
			----------
			2. WARNING in OverrideTest.java (at line 10)
				public void doSomethingElse(String text) // "Missing non-null annotation" warning
				                            ^^^^^^
			Missing non-null annotation: inherited method from UntypedBase specifies this parameter as @NonNull
			----------
			""");
}
// original test
public void testBug424624() {
	runConformTestWithLibs(
		new String[] {
			"Test3.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				
				public class Test3 {
				
					public Test3() {
					}
				
					static public class Test3aa extends Object {}
					static public final @NonNull Test3aa Test3a = new Test3aa();
				
				}
				""",
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false /*flush*/,
		new String[] {
			"Test4.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				
				public class Test4 {
				
					public Test4() {
					}
				\t
					public void test() {
						test1( Test3.Test3a);
					}
				\t
					public void test1( @NonNull Object object) {
					}
				
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// other nesting levels, binary case
public void testBug424624a() {
	runConformTestWithLibs(
		new String[] {
			"test/Test3.java",
			"package test;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			(this.complianceLevel >= ClassFileConstants.JDK1_8 ?
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.TYPE_USE) @interface Marker {}\n"
			:
			""
			)+
			"\n" +
			"public class Test3 {\n" +
			"\n" +
			"	public Test3() {\n" +
			"	}\n" +
			"\n" +
			"	public class Inner extends Object {\n" +
			"		class DeepInner {}\n" +
			"	}\n" +
			"	public static class Nested extends Object {\n" +
			"		class InnerInNested {}\n" +
			"		static class DeepNested {}\n" +
			"	}\n" +
			"	static public final @NonNull Inner field1 = new Test3().new Inner();\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"	static public final @NonNull Inner.DeepInner field2 = field1.new DeepInner();\n" +
			"	static public final @NonNull Nested.InnerInNested field3 = new Nested().new InnerInNested();\n" +
			"	static public final @NonNull Nested.DeepNested field4 = new Nested.DeepNested();\n"
			:
			"	static public final @Marker Inner.@NonNull DeepInner field2 = field1.new DeepInner();\n" +
			"	static public final Nested.@NonNull InnerInNested field3 = new Nested().new InnerInNested();\n" +
			"	static public final Nested.@NonNull DeepNested field4 = new Nested.DeepNested();\n"
			) +
			"\n" +
			"}\n",
		},
		getCompilerOptions(),
		"");
	runConformTestWithLibs(
		false /*flush*/,
		new String[] {
			"Test4.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				import test.Test3;
				
				public class Test4 {
				
					public Test4() {
					}
				\t
					public void test() {
						test1( Test3.field1);
						test1( Test3.field2);
						test1( Test3.field3);
						test1( Test3.field4);
					}
				\t
					public void test1( @NonNull Object object) {
					}
				
				}
				"""
		},
		getCompilerOptions(),
		"");
}
// same as previous, source case for reference
public void testBug424624b() {
	runConformTestWithLibs(
		new String[] {
			"Test3.java",
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			(this.complianceLevel >= ClassFileConstants.JDK1_8 ?
			"import java.lang.annotation.*;\n" +
			"@Target(ElementType.TYPE_USE) @interface Marker {}\n"
			:
			""
			)+
			"\n" +
			"public class Test3 {\n" +
			"\n" +
			"	public Test3() {\n" +
			"	}\n" +
			"\n" +
			"	public class Inner extends Object {\n" +
			"		class DeepInner {}\n" +
			"	}\n" +
			"	public static class Nested extends Object {\n" +
			"		class InnerInNested {}\n" +
			"		static class DeepNested {}\n" +
			"	}\n" +
			"	static public final @NonNull Inner field1 = new Test3().new Inner();\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_8 ?
			"	static public final @NonNull Inner.DeepInner field2 = field1.new DeepInner();\n" +
			"	static public final @NonNull Nested.InnerInNested field3 = new Nested().new InnerInNested();\n" +
			"	static public final @NonNull Nested.DeepNested field4 = new Nested.DeepNested();\n"
			:
			"	static public final @Marker Inner.@NonNull DeepInner field2 = field1.new DeepInner();\n" +
			"	static public final Nested.@NonNull InnerInNested field3 = new Nested().new InnerInNested();\n" +
			"	static public final Nested.@NonNull DeepNested field4 = new Nested.DeepNested();\n"
			) +
			"\n" +
			"}\n",
			"Test4.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				
				public class Test4 {
				
					public Test4() {
					}
				\t
					public void test() {
						test1( Test3.field1);
						test1( Test3.field2);
						test1( Test3.field3);
						test1( Test3.field4);
					}
				\t
					public void test1( @NonNull Object object) {
					}
				
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug430084() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				@NonNullByDefault
				public class X {\
					Y() {} \
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				public class X {	Y() {} }
				                	^^^
			Return type for the method is missing
			----------
			""",
		this.LIBS,
		true, /*flush*/
		null /*options*/);
}
public void testBug432348() {
	String sourceString =
		"""
		import org.eclipse.jdt.annotation.NonNull;
		import java.lang.annotation.*;
		
		@Target(ElementType.FIELD)
		@interface Marker {}
		public enum E {
			@Marker @NonNull A, B, C
		}
		""";
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTestWithLibs(
			new String[] {
				"E.java",
				sourceString
			},
			getCompilerOptions(),
			"");
	} else {
		runNegativeTestWithLibs(
			new String[] {
				"E.java",
				sourceString
			},
			"""
				----------
				1. ERROR in E.java (at line 7)
					@Marker @NonNull A, B, C
					        ^^^^^^^^
				Syntax error, type annotations are illegal here
				----------
				""");
	}
}
// Bug 403674 - [compiler][null] Switching on @Nullable enum value does not trigger "Potential null pointer access" warning
// String value being used in switch condition.
public void testBug403674() {
	Map options = getCompilerOptions();
	runNegativeTestWithLibs(
			new String[]{
				"X.java",
				"""
					import org.eclipse.jdt.annotation.Nullable;
					public class X {
					   public static void main(String[] args) {
					      // Correctly flagged as "Potential null pointer access."
					      switch (computeStringValue()) {}
					   }
					   private static @Nullable String computeStringValue() { return null; }
					}
					"""
			},
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 5)\n" +
			"	switch (computeStringValue()) {}\n" +
			"	        ^^^^^^^^^^^^^^^^^^^^\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_7
			?
			"Cannot switch on a value of type String for source level below 1.7. " +
			"Only convertible int values or enum variables are permitted\n"
			:
			"Potential null pointer access: The method computeStringValue() may return null\n"
			) +
			"----------\n");
}
// Bug 403674 - [compiler][null] Switching on @Nullable enum value does not trigger "Potential null pointer access" warning
// Enum value being used in switch condition.
public void testBug403674a() {
	Map options = getCompilerOptions();
	runNegativeTestWithLibs(
			new String[]{
				"X.java",
				"""
					import org.eclipse.jdt.annotation.Nullable;
					public class X {
					   private enum EnumValue{}
					   public static void main(String[] args) {
					      // Before Fix: Not flagged.
					      switch (computeEnumValue()) {}
					      @Nullable EnumValue value = computeEnumValue();
					      // Correctly flagged as "Potential null pointer access."
					      // Before Fix: Not flagged.
					      switch (value) {}
					   }
					   private static @Nullable EnumValue computeEnumValue() { return null; }
					}
					"""
			},
			options,
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	switch (computeEnumValue()) {}\n" +
			"	        ^^^^^^^^^^^^^^^^^^\n" +
			"Potential null pointer access: The method computeEnumValue() may return null\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 10)\n" +
			"	switch (value) {}\n" +
			"	        ^^^^^\n" +
			(this.complianceLevel < ClassFileConstants.JDK1_8
			?
			"Potential null pointer access: The variable value may be null at this location\n"
			:
			"Potential null pointer access: this expression has a '@Nullable' type\n"
			) +
			"----------\n");
}
// original test
public void testBug422796() {
	runConformTestWithLibs(
		new String[] {
			"NullExprTest.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				
				@NonNullByDefault
				public class NullExprTest {
				\t
					private @Nullable Boolean b() { return null; }
				\t
					public void testBoolean() {
						Boolean b1 = b();
						boolean b = b1 == null ||\s
								b1; // <-- Previously bugggy: reported potential NPE (*)
						assertTrue(b);
					}
					static void assertTrue(boolean b) {}
				
				}"""
		},
		getCompilerOptions(),
		"");
}
// inverted logic:
public void testBug422796a() {
	runConformTestWithLibs(
		new String[] {
			"NullExprTest.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;
				
				@NonNullByDefault
				public class NullExprTest {
				\t
					private @Nullable Boolean b() { return null; }
				\t
					public void testBoolean() {
						Boolean b1 = b();
						boolean b = b1 != null &&\s
								b1; // <-- Previously bugggy: reported potential NPE (*)
						assertTrue(b);
					}
					static void assertTrue(boolean b) {}
				
				}"""
		},
		getCompilerOptions(),
		"");
}
// negative tests:
public void testBug422796b() {
	runNegativeTestWithLibs(
		new String[] {
			"NullExprTest.java",
			"""
				public class NullExprTest {
				\t
					private Boolean b() { return null; }
				\t
					public void testBoolean1() {
						Boolean b1 = b();
						boolean b = b1 == null &&\s
								b1; // <-- definite NPE (*)
						assertTrue(b);
					}
					public void testBoolean2(boolean x) {
						Boolean b1 = b();
						boolean b = (b1 == null || x) &&\s
								b1; // <-- potential NPE (*)
						assertTrue(b);
					}
					static void assertTrue(boolean b) {}
				
				}"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in NullExprTest.java (at line 8)
				b1; // <-- definite NPE (*)
				^^
			Null pointer access: This expression of type Boolean is null but requires auto-unboxing
			----------
			2. ERROR in NullExprTest.java (at line 14)
				b1; // <-- potential NPE (*)
				^^
			Potential null pointer access: This expression of type Boolean may be null but requires auto-unboxing
			----------
			""");
}
public void testBug434374() {
	runConformTestWithLibs(
		new String[] {
			"bal/AdapterService.java",
			"""
				/*******************************************************************************
				 * Copyright (c) 2013 BestSolution.at and others.
				 * All rights reserved. This program and the accompanying materials
				 * are made available under the terms of the Eclipse Public License v1.0
				 * which accompanies this distribution, and is available at
				 * http://www.eclipse.org/legal/epl-v10.html
				 *
				 * Contributors:
				 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
				 *******************************************************************************/
				package bal;
				
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				public interface AdapterService {
					public boolean canAdapt(@Nullable Object sourceObject, @NonNull Class<?> targetType);
				
					@Nullable
					public <A> A adapt(@Nullable Object sourceObject, @NonNull Class<A> targetType, ValueAccess... valueAccesses);
				
					public interface ValueAccess {
						@Nullable
						public <O> O getValue(@NonNull String key);
				
						@Nullable
						public <O> O getValue(@NonNull Class<O> key);
					}
				}
				""",
			"bal/AdapterServiceImpl.java",
			"/*******************************************************************************\n" +
			" * Copyright (c) 2013 BestSolution.at and others.\n" +
			" * All rights reserved. This program and the accompanying materials\n" +
			" * are made available under the terms of the Eclipse Public License v1.0\n" +
			" * which accompanies this distribution, and is available at\n" +
			" * http://www.eclipse.org/legal/epl-v10.html\n" +
			" *\n" +
			" * Contributors:\n" +
			" *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation\n" +
			" *******************************************************************************/\n" +
			"package bal;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"public class AdapterServiceImpl implements AdapterService {\n" +
			"\n" +
			(this.complianceLevel >= ClassFileConstants.JDK1_6
			? "	@Override\n"
			: "") +
			"	public boolean canAdapt(@Nullable Object sourceObject, @NonNull Class<?> targetType) {\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			(this.complianceLevel >= ClassFileConstants.JDK1_6
			? "	@Override\n"
			: "") +
			"	@Nullable\n" +
			"	public <A> A adapt(@Nullable Object sourceObject, @NonNull Class<A> targetType, ValueAccess... valueAccesses) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// test return type compatibility
public void testBug434374a() {
	runConformTestWithLibs(
		new String[] {
			"bug434374/AdapterService.java",
			"""
				package bug434374;
				
				import org.eclipse.jdt.annotation.*;
				
				public interface AdapterService {
					public @NonNull <A> Class<A> getClassOfA(A object);
				
				}
				""",
			"bug434374/AdapterServiceImpl.java",
			"package bug434374;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public class AdapterServiceImpl implements AdapterService {\n" +
			"\n" +
			(this.complianceLevel >= ClassFileConstants.JDK1_6
			? "	@Override\n"
			: "") +
			"	@NonNull\n" +
			"	public <A> Class<A> getClassOfA(A object) {\n" +
			"		throw new RuntimeException();\n" +
			"	}\n" +
			"\n" +
			"}\n"
		},
		getCompilerOptions(),
		"");
}
// original (broken) test (second part):
public void testBug434374b() {
	runNegativeTestWithLibs(
		new String[] {
			"bal/TestGeneric.java",
			"""
				package bal;
				import org.eclipse.jdt.annotation.NonNull;
				
				public class TestGeneric<T> {
					@NonNull
					public T test() {
						return null;
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in bal\\TestGeneric.java (at line 7)
				return null;
				       ^^^^
			Null type mismatch: required \'@NonNull T\' but the provided value is null
			----------
			""");
}
// rectified test:
public void testBug434374c() {
	runConformTestWithLibs(
		new String[] {
			"bal/TestGeneric.java",
			"""
				package bal;
				import org.eclipse.jdt.annotation.Nullable;
				
				public class TestGeneric<T> {
					public @Nullable T test() {
						return null;
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}

// @NNBD should not affect implicit constructor
public void testBug443347() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				abstract class Super {
				  Super(String s) { }
				  abstract void bar();
				  void foo() { bar(); }
				}
				
				@NonNullByDefault
				public class X {
				  void test1(@Nullable String s) {
				    new Super(s) {
				      @Override
				      void bar() {}
				    }.foo();
				  }
				}
				"""
		},
		getCompilerOptions(),
		"");
}

// explicit annotation on super ctor should be inherited
public void testBug443347b() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				abstract class Super {
				  Super(@NonNull String s) { }
				  abstract void bar();
				  void foo() { bar(); }
				}
				
				@NonNullByDefault
				public class X {
				  void test1(@Nullable String s) {
				    new Super(s) {
				      @Override
				      void bar() {}
				    }.foo();
				  }
				}
				"""
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 11)\n" +
		"	new Super(s) {\n" +
		"	          ^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n"
		: "Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n") +
		"----------\n");
}

// @NNBD on super ctor should be inherited
public void testBug443347c() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				abstract class Super {
				  Super(String s) { }
				  abstract void bar();
				  void foo() { bar(); }
				}
				
				@NonNullByDefault
				public class X {
				  void test1(@Nullable String s) {
				    new Super(s) {
				      @Override
				      void bar() {}
				    }.foo();
				  }
				}
				"""
		},
		getCompilerOptions(),
		"----------\n" +
		"1. ERROR in X.java (at line 12)\n" +
		"	new Super(s) {\n" +
		"	          ^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Null type mismatch: required \'@NonNull String\' but the provided value is specified as @Nullable\n"
		: "Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'\n") +
		"----------\n");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444024, Type mismatch error in annotation generics assignment which happens "sometimes"
public void test444024() {
		this.runConformTest(
		   new String[] {
			   "ViewpointOrganisationEntity.java",
			   "abstract public class ViewpointOrganisationEntity<T> {\n" +
			   "}\n",
			   "MetaCombo.java",
			   """
				public @interface MetaCombo {
					Class< ? extends IComboDataSet< ? >> dataSet();
				}
				""",
			   "IComboDataSet.java",
			   "public interface IComboDataSet<T> {\n" +
			   "}\n",
			   "ContractantTypeLister.java",
			   "public class ContractantTypeLister implements IComboDataSet<ContractantType> {\n" +
			   "}\n",
			   "ContractantType.java",
			   """
				@MetaCombo(dataSet = ContractantTypeLister.class)
				public class ContractantType extends ViewpointOrganisationEntity<Long>  {
				}
				""",
		       "Contractant.java",
		       """
				public class Contractant extends ViewpointOrganisationEntity<Long> {
					@MetaCombo(dataSet = ContractantTypeLister.class)
					public ContractantType getContractantType() {
						return null;
					}
				}
				""",
		   },
		   "");
}
public void testBug435805() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	options.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runNegativeTest(
		true/*flush*/,
		new String[] {
			"org/foo/Nullable.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
				public @interface Nullable {}
				""",
			"org/foo/NonNull.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
				public @interface NonNull {}
				""",
			"TestNulls.java",
			"""
				import org.foo.*;
				
				public class TestNulls {
					public void testCase(@Nullable String theValue) {
						int len = theValue.length();					// Is nullable, so should report error here.
					}
				
				}"""
		},
		null/*libs*/,
		options,
		"""
			----------
			1. ERROR in TestNulls.java (at line 5)
				int len = theValue.length();					// Is nullable, so should report error here.
				          ^^^^^^^^
			Potential null pointer access: The variable theValue may be null at this location
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public void testBug445147() {
	runConformTestWithLibs(
		new String[] {
			"foobar/Bar.java",
			"""
				package foobar;
				@org.eclipse.jdt.annotation.NonNullByDefault
				interface Bar<B extends Bar<B, F>, F extends Foo<F, B>> {}""",
			"foobar/Foo.java",
			"""
				package foobar;
				@org.eclipse.jdt.annotation.NonNullByDefault
				interface Foo<F extends Foo<F, B>, B extends Bar<B, F>> {}"""
		},
		getCompilerOptions(),
		"");
}
public void testBug445708() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // uses switch on string.
	runNegativeTestWithLibs(
		new String[] {
			"SwitchTest.java",
			"""
				import org.eclipse.jdt.annotation.Nullable;
				
				public class SwitchTest
				{
				   private enum EnumValue
				   {
				   }
				  \s
				   public static void main(String[] args)
				   {
				      // Should be flagged as "Potential null pointer access," but is not.
				      switch (computeStringValue())
				      {
				      }
				     \s
				      @Nullable String stringValue = null;
				     \s
				      // Properly flagged as "Null pointer access."
				      switch (stringValue)
				      {
				      }
				     \s
				      stringValue = computeStringValue();
				     \s
				      // Should be flagged as "Potential null pointer access," but is not.
				      switch (stringValue)
				      {
				      }
				     \s
				      // Should also be flagged, but is not.
				      switch (computeEnumValue())
				      {
				      }
				     \s
				      @Nullable EnumValue enumValue = null;
				     \s
				      // Fixed in bug #403674.
				      switch (enumValue)
				      {
				      }
				   }
				  \s
				   private static @Nullable String computeStringValue()
				   {
				      return null;
				   }
				  \s
				   private static @Nullable EnumValue computeEnumValue()
				   {
				      return null;
				   }
				}
				"""
		},
		"----------\n" +
		"1. ERROR in SwitchTest.java (at line 12)\n" +
		"	switch (computeStringValue())\n" +
		"	        ^^^^^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The method computeStringValue() may return null\n" +
		"----------\n" +
		"2. ERROR in SwitchTest.java (at line 19)\n" +
		"	switch (stringValue)\n" +
		"	        ^^^^^^^^^^^\n" +
		"Null pointer access: The variable stringValue can only be null at this location\n" +
		"----------\n" +
		"3. ERROR in SwitchTest.java (at line 26)\n" +
		"	switch (stringValue)\n" +
		"	        ^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Potential null pointer access: The variable stringValue may be null at this location\n"
		: "Potential null pointer access: this expression has a \'@Nullable\' type\n" ) +
		"----------\n" +
		"4. ERROR in SwitchTest.java (at line 31)\n" +
		"	switch (computeEnumValue())\n" +
		"	        ^^^^^^^^^^^^^^^^^^\n" +
		"Potential null pointer access: The method computeEnumValue() may return null\n" +
		"----------\n" +
		"5. ERROR in SwitchTest.java (at line 38)\n" +
		"	switch (enumValue)\n" +
		"	        ^^^^^^^^^\n" +
		"Null pointer access: The variable enumValue can only be null at this location\n" +
		"----------\n");
}
// same as above but 1.8 with declaration annotations
public void testBug445708b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // only one combination tested
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	runNegativeTestWithLibs(
		new String[] {
			CUSTOM_NULLABLE_NAME,
			CUSTOM_NULLABLE_CONTENT,
			"SwitchTest.java",
			"""
				import org.eclipse.jdt.annotation.Nullable;
				
				public class SwitchTest
				{
				   private enum EnumValue
				   {
				   }
				  \s
				   public static void main(String[] args)
				   {
				      // Should be flagged as "Potential null pointer access," but is not.
				      switch (computeStringValue())
				      {
				      }
				     \s
				      @Nullable String stringValue = null;
				     \s
				      // Properly flagged as "Null pointer access."
				      switch (stringValue)
				      {
				      }
				     \s
				      stringValue = computeStringValue();
				     \s
				      // Should be flagged as "Potential null pointer access," but is not.
				      switch (stringValue)
				      {
				      }
				     \s
				      // Should also be flagged, but is not.
				      switch (computeEnumValue())
				      {
				      }
				     \s
				      @Nullable EnumValue enumValue = null;
				     \s
				      // Fixed in bug #403674.
				      switch (enumValue)
				      {
				      }
				   }
				  \s
				   private static @Nullable String computeStringValue()
				   {
				      return null;
				   }
				  \s
				   private static @Nullable EnumValue computeEnumValue()
				   {
				      return null;
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in SwitchTest.java (at line 12)
				switch (computeStringValue())
				        ^^^^^^^^^^^^^^^^^^^^
			Potential null pointer access: The method computeStringValue() may return null
			----------
			2. ERROR in SwitchTest.java (at line 19)
				switch (stringValue)
				        ^^^^^^^^^^^
			Null pointer access: The variable stringValue can only be null at this location
			----------
			3. ERROR in SwitchTest.java (at line 26)
				switch (stringValue)
				        ^^^^^^^^^^^
			Potential null pointer access: this expression has a \'@Nullable\' type
			----------
			4. ERROR in SwitchTest.java (at line 31)
				switch (computeEnumValue())
				        ^^^^^^^^^^^^^^^^^^
			Potential null pointer access: The method computeEnumValue() may return null
			----------
			5. ERROR in SwitchTest.java (at line 38)
				switch (enumValue)
				        ^^^^^^^^^
			Null pointer access: The variable enumValue can only be null at this location
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=452780 - Internal compiler error: arrayIndexOutOfBounds
public void testBug452780() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return;
	runConformTestWithLibs(
		new String[] {
			"Tools2.java",
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.Set;
				import java.util.stream.Collector;
				import java.util.stream.Collectors;
				import org.eclipse.jdt.annotation.NonNull;
				public class Tools2 {
					@SafeVarargs
					public static <T> List<@NonNull T> asList(T... ts) {
						@SuppressWarnings("null")
						@NonNull
						List<@NonNull T> res = Arrays.asList(ts);
						return res;
					}
					@SuppressWarnings("null")
					public static <T> Collector<@NonNull T, @NonNull ?, @NonNull Set<@NonNull T>> toSet() {
						@NonNull
						Collector<@NonNull T, ?, @NonNull Set<@NonNull T>> res = Collectors
								.toSet();
						return res;
					}
				}"""
		},
		getCompilerOptions(),
		"");
}
public void testBug455557() {
	runWarningTestWithLibs(
		true, /*flush*/
		new String[] {
			"X.java",
			"""
				import java.util.List;
				
				import org.eclipse.jdt.annotation.NonNull;
				
				
				public class X {
					void test(List<String> list, boolean b) {
						if (b) {
							while (true) {
								for (@NonNull Object y : list) {\s
								}
							}
						}
					}
				}
				"""
		},
		null,
		"----------\n" +
		"1. WARNING in X.java (at line 10)\n" +
		"	for (@NonNull Object y : list) { \n" +
		"	                         ^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Null type safety: The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'\n"
		: "Null type safety (type annotations): The expression of type \'String\' needs unchecked conversion to conform to \'@NonNull Object\'\n"
		) +
		"----------\n");
}
public void testBug455723() {
	runConformTestWithLibs(
		new String[] {
			"Problem.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Problem {
					public void fubar(final @Nullable String arg) {
						if (arg == null) {
							return;
						}
					\t
						doSomething(arg);
						// no errors here
					\t
						while (true) {\t
							doSomething(arg);
							//          ^^^  compiler error
						}
					}
				\t
					private void doSomething(@NonNull String arg) {	}
				}
				"""
		},
		null,
		"");
}
public void testBug455723b() {
	runConformTestWithLibs(
		new String[] {
			"Problem.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class Problem {
					public void fubar(final @Nullable String arg) {
						if (arg == null) {
							return;
						}
						@NonNull String local;
					\t
						while (true) {\t
							local = arg;
						}
					}
				}
				"""
		},
		null,
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=436486
public void test_null_with_apt() {
	boolean apt = this.enableAPT;
	this.enableAPT = true;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.WARNING);
	customOptions.put(JavaCore.COMPILER_PB_UNUSED_WARNING_TOKEN, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"NullWarn.java",
			"""
				@SuppressWarnings("null")
				public class NullWarn {
				
				    // Some code
				
				}
				"""
		},
		customOptions,
		"");
	this.enableAPT = apt;
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=436486#c4
public void test_null_with_apt_comment4() {
	boolean apt = this.enableAPT;
	this.enableAPT = true;
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_UNUSED_WARNING_TOKEN, JavaCore.ERROR);
	runWarningTestWithLibs(
		true, // flush
		new String[] {
			"Test.java",
			"""
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public class Test {
				
					public static final Test t = new Test(Integer.valueOf(0));
				
					public Test(Integer integer) {
					\t
					}
				}
				"""
		},
		customOptions,
		"----------\n" +
		"1. WARNING in Test.java (at line 6)\n" +
		"	public static final Test t = new Test(Integer.valueOf(0));\n" +
		"	                                      ^^^^^^^^^^^^^^^^^^\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		? "Null type safety: The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Integer\'\n"
		: "Null type safety (type annotations): The expression of type \'Integer\' needs unchecked conversion to conform to \'@NonNull Integer\'\n"
		) +
		"----------\n");
	this.enableAPT = apt;
}
public void testBug457210() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo.NonNull");
	runNegativeNullTest(
		new String[] {
			"org/foo/NonNull.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				public @interface NonNull {
				}
				""",
			"org/foo/Nullable.java",
			"""
				package org.foo;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				public @interface Nullable {
				}
				""",
			"TestRunner.java",
			"""
				import org.foo.*;
				public class TestRunner {
					private TestRunner() {}
				
					@Nullable
					OutputHelper m_outputHelper;
					int foo(@NonNull OutputHelper helper) { return helper.i; }
				}
				""",
			"OutputHelper.java",
			"""
				@org.foo.NonNull public class OutputHelper {
					public int i;
				}
				"""
		},
		"""
			----------
			1. ERROR in OutputHelper.java (at line 1)
				@org.foo.NonNull public class OutputHelper {
				^^^^^^^^^^^^^^^^
			The nullness annotation \'NonNull\' is not applicable at this location
			----------
			""",
		null,
		true,
		customOptions);
}
public void testBug462790() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return; // multi catch used
	Map<String,String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runWarningTestWithLibs(
		true, /*flush*/
		new String[] {
			"EclipseBug.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class EclipseBug {
				
					public void method(Class<? extends String> commandType) {
						String command = (String)getCommand(commandType);
					}
				\t
					public static <T extends String> T getCommand(Class<T> commandType) {
						try {
							return commandType.newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException();
						}
					}
				}"""
		},
		options,
		"----------\n" +
		"1. WARNING in EclipseBug.java (at line 5)\n" +
		"	String command = (String)getCommand(commandType);\n" +
		"	                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unnecessary cast from capture#1-of ? extends String to String\n" +
		"----------\n" +
		"2. WARNING in EclipseBug.java (at line 8)\n" +
		"	public static <T extends String> T getCommand(Class<T> commandType) {\n" +
		"	                         ^^^^^^\n" +
		"The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" +
		"----------\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		?
		"3. WARNING in EclipseBug.java (at line 10)\n" +
		"	return commandType.newInstance();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Null type safety: The expression of type \'T\' needs unchecked conversion to conform to \'@NonNull T\'\n" +
		"----------\n"
		:
		"3. INFO in EclipseBug.java (at line 10)\n" +
		"	return commandType.newInstance();\n" +
		"	       ^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
		"Unsafe interpretation of method return type as \'@NonNull\' based on the receiver type \'@NonNull Class<T extends @NonNull String>\'. Type \'Class<T>\' doesn\'t seem to be designed with null type annotations in mind\n" +
		"----------\n"));
}
public void testBug459967_Enum_valueOf() {
	runConformTestWithLibs(
		new String[] {
			"MyEnum.java",
			"public enum MyEnum { V1, V2 }\n",
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNull MyEnum forString(String name) {
						return MyEnum.valueOf(name);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug459967_Enum_valueOf_binary() {
	runConformTest(
		new String[] {
			"MyEnum.java",
			"public enum MyEnum { V1, V2 }\n"
		});
	runConformTestWithLibs(
		false /*flush*/,
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					@NonNull MyEnum forString(String name) {
						return MyEnum.valueOf(name);
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug459967_Enum_values() {
	String[] testFiles = new String[] {
		"MyEnum.java",
		"public enum MyEnum { V1, V2 }\n",
		"X.java",
		"import org.eclipse.jdt.annotation.*;\n" +
		"public class X {\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		?
		"	@NonNull MyEnum[] getValues() {\n"
		:
		"	@NonNull MyEnum @NonNull[] getValues() {\n"
		)+
		"		return MyEnum.values();\n" +
		"	}\n" +
		"	void printAll() {\n" +
		"		for (@NonNull MyEnum value : MyEnum.values())\n" +
		"			System.out.println(value);\n" +
		"	}\n" +
		"}\n"
	};
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTestWithLibs(
				testFiles,
				getCompilerOptions(),
				"""
					----------
					1. WARNING in X.java (at line 7)
						for (@NonNull MyEnum value : MyEnum.values())
						                             ^^^^^^^^^^^^^^^
					Null type safety: The expression of type \'MyEnum\' needs unchecked conversion to conform to \'@NonNull MyEnum\'
					----------
					""");
	} else {
		runConformTestWithLibs(
				testFiles,
				getCompilerOptions(),
				"");
	}
}
public void testBug459967_Enum_values_binary() {
	String[] testFiles = new String[] {
		"X.java",
		"import org.eclipse.jdt.annotation.*;\n" +
		"public class X {\n" +
		(this.complianceLevel < ClassFileConstants.JDK1_8
		?
		"	@NonNull MyEnum[] getValues() {\n"
		:
		"	@NonNull MyEnum @NonNull[] getValues() {\n"
		)+
		"		return MyEnum.values();\n" +
		"	}\n" +
		"	void printAll() {\n" +
		"		for (@NonNull MyEnum value : MyEnum.values())\n" +
		"			System.out.println(value);\n" +
		"	}\n" +
		"}\n"
	};
	runConformTest(
		new String[] {
			"MyEnum.java",
			"public enum MyEnum { V1, V2 }\n",
		});
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		runConformTestWithLibs(
				false /*flush*/,
				testFiles,
				getCompilerOptions(),
				"""
					----------
					1. WARNING in X.java (at line 7)
						for (@NonNull MyEnum value : MyEnum.values())
						                             ^^^^^^^^^^^^^^^
					Null type safety: The expression of type \'MyEnum\' needs unchecked conversion to conform to \'@NonNull MyEnum\'
					----------
					""");
	} else {
		runConformTestWithLibs(
				false /*flush*/,
				testFiles,
				getCompilerOptions(),
				"");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=407414
// Incorrect warning on a primitive type being null.
public void test407414a()  {
	 String testCode = """
		package p1;
		public class Test {
			void fooI(int i) {\s
				barI(i);
			}
			void fooB(boolean i) {
				barB(i);
			}
			void fooBy(byte i) {
				barBy(i);
			}
			void fooF(float i) {
				barF(i);
			}
			void fooL(long i) {
				barL(i);
			}
			void fooC(char i) {
				barC(i);
			}
			void fooS(short i) {
				barS(i);
			}
			static void barI(Integer i) {}
			static void barB(Boolean i) {}
			static void barBy(Byte i) {}
			static void barF(Float i) {}
			static void barL(Long i) {}
			static void barC(Character i) {}
			static void barS(Short i) {}
		}""";
	 String pcode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			 "package p1;";
	 runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			pcode,
			"p1/Test.java",
			testCode
		},
		getCompilerOptions(),
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=407414
// Incorrect warning on a primitive type being null.
// The information that boxing is happening at i2 = i
// and therefore there cannot be null values in i2 is
// not flowing down to access of i2.
// The test case also illustrates array access and Qualified access.
public void test407414b() {
	 String testCode = """
		package p1;
		  public class Test {
		  class Y {
				class Z {
					int i;
		          int a[];
		      	Z() {
						a = new int[0];
		      	}
				}
		  }
			void foo(int i) {
				Integer i2 = i;
				bar(i2);
			}
			void fooA(int a[], int i) {
				Integer i2 = a[i];
				bar(i2);
			}
		  void fooQ(Y.Z yz, int i) {
				Integer i2 = yz.i;
				bar(i2);
		      i2 = yz.a[i];
		      bar(i2);
		  }
			static void bar(Integer i) { }
		}""";
	 String pcode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			 "package p1;";
	 runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			pcode,
			"p1/Test.java",
			testCode
		},
		getCompilerOptions(),
		"");
}

public void test407414b2() {
	 String testCode = """
		package p1;
		  public class Test {
		  int a[];
		  Test() {
				a = new int[0];
		      a[0] = 0;
		  }
			void fooA(int i) {
				Integer i2 = a[i];
				bar(i2);
			}
			static void bar(Integer i) { }
		}""";
	 String pcode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			 "package p1;";
	 runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			pcode,
			"p1/Test.java",
			testCode
		},
		getCompilerOptions(),
		"");
}

// FieldReference.
public void test407414b3() {
	 String testCode = """
		package p1;
		public class Test {
		  class Z {
				int a[];
				Z() {
			  		a = new int[0];
			  		a[0] = 0;
				}
		  }
		  class Y {
				Z[] z;
				Y () {
			 		z = new Z[0];
				}
		  }
		  void fooQ(Y y, int i) {
				Integer i2 = y.z[i].a[i];
				bar(i2);
		  }
		  static void bar(Integer i) { }
		}""";
	 String pcode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			 "package p1;";
	 runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			pcode,
			"p1/Test.java",
			testCode
		},
		getCompilerOptions(),
		"");
}

// arrayRefrence
public void test407414b4() {
	 String testCode = """
		package p1;
		public class Test {
		  class Y {
				int a[];
				Y() {
				  a = new int[0];
				  a[0] = 0;
				}
		  }
		  void fooQ(Y[] y, int i) {
				Integer i2 = y[i].a[i];
				bar(i2);
		  }
		  static void bar(Integer i) { }
		}""";
	 String pcode = "@org.eclipse.jdt.annotation.NonNullByDefault\n" +
			 "package p1;";
	 runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			pcode,
			"p1/Test.java",
			testCode
		},
		getCompilerOptions(),
		"");
}

// value of a (compound) assignment
public void testBug407414c() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
				  int fI;
				  @org.eclipse.jdt.annotation.NonNull Integer test1(int i) {
						return fI = i;
				  }
				  @org.eclipse.jdt.annotation.NonNull Integer test2(int i) {
						return fI += i;
				  }
				}
				"""
		},
		getCompilerOptions(),
		"");
}

// primitive cast
public void testBug407414d() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
				  @org.eclipse.jdt.annotation.NonNull Long test(int i) {
						return (long)i;
				  }
				}
				"""
		},
		getCompilerOptions(),
		"");
}

// conditional
public void testBug407414e() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
				  @org.eclipse.jdt.annotation.NonNull Long test(long l, boolean b) {
						return b ? l : 3;
				  }
				}
				"""
		},
		getCompilerOptions(),
		"");
}

// operators
public void testBug407414f() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				public class X {
				  @org.eclipse.jdt.annotation.NonNull Long test1(long l) {
						return l + 3;
				  }
				  @org.eclipse.jdt.annotation.NonNull Long test2(long l) {
						return l << 3;
				  }
				  @org.eclipse.jdt.annotation.NonNull Long test3(long l) {
						return l++;
				  }
				  @org.eclipse.jdt.annotation.NonNull Long test4(long l) {
						return -l;
				  }
				}
				"""
		},
		getCompilerOptions(),
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428104
// Null annotation heuristics does not understand autoboxed primitives to be non-null.
public void test428104() {
	 String testCode = """
		package p1;
		import org.eclipse.jdt.annotation.NonNull;
		public class Test {
		    @NonNull
		    Boolean case1Parent() {
		        return case1Child();
		    }
		    boolean case1Child() {
		        return Math.random() > 0.5;
		    }
		}
		""";
	 String pcode = "package p1;";
	 runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			pcode,
			"p1/Test.java",
			testCode
		},
		getCompilerOptions(),
		"");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424702
// Warning at an assignment of a boolean-Variable to an Boolean-Variable
public void test424702() {
	 String testCode = """
		package p1;
		import org.eclipse.jdt.annotation.NonNull;
		public class Test {
		    private @NonNull Boolean t = true;
		    Boolean foo() {
				boolean y = false;
		      t = y;
				return t;
		    }
		}
		""";
	 String pcode = "package p1;";
	 runConformTestWithLibs(
		new String[] {
			"p1/package-info.java",
			pcode,
			"p1/Test.java",
			testCode
		},
		getCompilerOptions(),
		"");
}

public void testBug237236() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class X {
				  public void x(Long l) {}
				  public long z() { return 0L; }
				  public void y() { x(z()); }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug418236() {
	runConformTestWithLibs(
		new String[] {
			"MyClass.java",
			"""
				@org.eclipse.jdt.annotation.NonNullByDefault
				public class MyClass {
				  private static final int CONSTANT = 24;
				
				  public Integer returnConstant() {
				    return CONSTANT; // <-- incorrect error. Integer.valueOf is declared as non-null.
				  }
				
				  public Integer returnInteger() {
				    return 24; // <-- no error reported here
				  }
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug461878() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "jakarta.annotation.Nonnull");
	runNegativeTest(
		true, /*flush*/
		new String[] {
			"jakarta/annotation/Nonnull.java",
			"""
				package jakarta.annotation;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				@Retention(RetentionPolicy.RUNTIME)
				public @interface Nonnull {
				}
				""",
			"edu/umd/cs/findbugs/annotations/PossiblyNull.java",
			"""
				package edu.umd.cs.findbugs.annotations;
				@jakarta.annotation.Nonnull // <-- error!!!
				public @interface PossiblyNull {
				}
				"""
		},
		null, /*libs*/
		compilerOptions,
		"""
			----------
			1. WARNING in edu\\umd\\cs\\findbugs\\annotations\\PossiblyNull.java (at line 2)
				@jakarta.annotation.Nonnull // <-- error!!!
				^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The nullness annotation \'Nonnull\' is not applicable at this location
			----------
			""",
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
public void testBug467610() {
	runConformTestWithLibs(
		new String[] {
			"SuperClass.java",
			"""
				import org.eclipse.jdt.annotation.*;
				@NonNullByDefault
				public abstract class SuperClass<T> {
				
					abstract T doSomething(T arg);
				
					abstract String returnAString();
				
					public static abstract class SubClass<S> extends SuperClass<S> {
				
						@Override
						abstract S doSomething(S arg);
				
						@Override
						abstract String returnAString();
					\t
					}
				
				}"""
		},
		getCompilerOptions(),
		"");
}
public void testBug477719() {
	runConformTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					void consume(@NonNull Class<? extends Number> c) {}
					void test(Double d) {
						consume(Integer.class);
						consume(d.getClass());
					}
				}
				"""
		},
		getCompilerOptions(),
		"");
}
public void testBug482075() {
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS, JavaCore.ENABLED);
	options.put(JavaCore.COMPILER_PB_NULL_UNCHECKED_CONVERSION, JavaCore.ERROR);
	runConformTestWithLibs(
		new String[] {
			"TestIncidentImports2.java",
			"""
				public class TestIncidentImports2 {
				
				    private String arg0;
				    private TestIncidentImports2 arg2;
				
				    public TestIncidentImports2(String arg0) {
				        this.arg0 = arg0;
				    }
				
				    protected void apply(Object o) throws Exception {
				        arg0.length();
				        other(arg0);
				        if (this.arg2.arg0 != null && other(arg2))
							System.out.println(9);
				    }
				
				    boolean other(@org.eclipse.jdt.annotation.NonNull Object o) {
						return true;
				    }
				}
				"""
		},
		options,
		""
	);
}
public void testMultipleAnnotations() {
	Map options1 = new HashMap<>(getCompilerOptions());
	options1.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo1.NonNull");
	options1.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo1.Nullable");
	runConformTest(
		new String[] {
			"org/foo1/Nullable.java",
			"""
				package org.foo1;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
				public @interface Nullable {}
				""",
			"org/foo1/NonNull.java",
			"""
				package org.foo1;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
				public @interface NonNull {}
				""",
			"p1/TestNulls.java",
			"""
				package p1;
				import org.foo1.*;
				
				public class TestNulls {
					public @Nullable String weaken(@NonNull String theValue) {
						return theValue;
					}
				
				}"""
		},
		options1);
	Map options2 = new HashMap<>(getCompilerOptions());
	options2.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "org.foo2.NonNull2");
	options2.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "org.foo2.Nullable2");
	options2.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "org.foo2.NoNulls2");
	options2.put(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.WARNING);
	runConformTest(
		false, // flush
		new String[] {
			"org/foo2/Nullable2.java",
			"""
				package org.foo2;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
				public @interface Nullable2 {}
				""",
			"org/foo2/NonNull2.java",
			"""
				package org.foo2;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
				public @interface NonNull2 {}
				""",
			"org/foo2/NoNulls2.java",
			"""
				package org.foo2;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.CLASS)
				@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
				public @interface NoNulls2 {}
				""",
			"p2/TestNulls2.java",
			"""
				package p2;
				import org.foo2.*;
				
				public class TestNulls2 {
					public @Nullable2 String weaken(@NonNull2 String theValue) {
						return theValue;
					}
					@NoNulls2
					public String strong(String theValue) {
						return weaken(theValue);
					}
				
				}""",
			"p2/TestNulls2a.java",
			"""
				package p2;
				import org.foo2.*;
				
				@NoNulls2
				public class TestNulls2a {
					public String strong(String theValue) {
						return theValue;
					}
				
				}"""
		},
		null, //libs
		options2,
		"""
			----------
			1. WARNING in p2\\TestNulls2.java (at line 10)
				return weaken(theValue);
				       ^^^^^^^^^^^^^^^^
			Null type mismatch: required \'@NonNull2 String\' but the provided value is specified as @Nullable2
			----------
			""",
		"",
		"",
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
	Map options3 = getCompilerOptions();
	options3.put(JavaCore.COMPILER_NONNULL_ANNOTATION_SECONDARY_NAMES, "org.foo1.NonNull,org.foo2.NonNull2");
	options3.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_SECONDARY_NAMES, " org.foo1.Nullable , org.foo2.Nullable2 "); // some spaces to test trimming
	options3.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_SECONDARY_NAMES, "org.foo2.NoNulls2");
	runNegativeTestWithLibs(
			new String[] {
				"p3/Test.java",
				"""
					package p3;
					import p1.TestNulls;
					import p2.TestNulls2;
					import p2.TestNulls2a;
					import org.eclipse.jdt.annotation.*;
					public class Test {
						@NonNull String test1(TestNulls test, @Nullable String input) {
							return test.weaken(input);
						}
						@NonNull String test2(TestNulls2 test, @Nullable String input) {
							return test.weaken(input);
						}
						@NonNull String test3(TestNulls2 test, @Nullable String input) {
							return test.strong(input); // requires nonnull due to method-level default
						}
						@NonNull String test4(TestNulls2a test, @Nullable String input) {
							return test.strong(input); // requires nonnull due to type-level default
						}
					}
					"""
			},
			options3,
				"----------\n" +
				"1. ERROR in p3\\Test.java (at line 8)\n" +
				"	return test.weaken(input);\n" +
				"	       ^^^^^^^^^^^^^^^^^^\n" +
				mismatch_NonNull_Nullable("String") +
				"----------\n" +
				"2. ERROR in p3\\Test.java (at line 8)\n" +
				"	return test.weaken(input);\n" +
				"	                   ^^^^^\n" +
				mismatch_NonNull_Nullable("String") +
				"----------\n" +
				"3. ERROR in p3\\Test.java (at line 11)\n" +
				"	return test.weaken(input);\n" +
				"	       ^^^^^^^^^^^^^^^^^^\n" +
				mismatch_NonNull_Nullable("String") +
				"----------\n" +
				"4. ERROR in p3\\Test.java (at line 11)\n" +
				"	return test.weaken(input);\n" +
				"	                   ^^^^^\n" +
				mismatch_NonNull_Nullable("String") +
				"----------\n" +
				"5. ERROR in p3\\Test.java (at line 14)\n" +
				"	return test.strong(input); // requires nonnull due to method-level default\n" +
				"	                   ^^^^^\n" +
				mismatch_NonNull_Nullable("String") +
				"----------\n" +
				"6. ERROR in p3\\Test.java (at line 17)\n" +
				"	return test.strong(input); // requires nonnull due to type-level default\n" +
				"	                   ^^^^^\n" +
				mismatch_NonNull_Nullable("String") +
				"----------\n");
}

public void testBug489486conform() {
	runConformTestWithLibs(
		new String[] {
			"test/DurationAdapter.java",
			"""
				package test;
				
				final class DurationAdapter extends java.lang.ref.SoftReference<String> {
					public DurationAdapter(String referent) {
						super(referent);
					}
				}
				""",
			"test/TheAnnotation.java",
			"""
				package test;
				
				public @interface TheAnnotation {
					Class<? extends java.lang.ref.SoftReference<?>> value();
				}
				""",
			"test/package-info.java",
			"@TheAnnotation(value = DurationAdapter.class)\n" +
			"package test;\n",
		},
		getCompilerOptions(),
		""
	);
}

public void testBug489486negative() {
	runNegativeTest(
		new String[] {
			"test/DurationAdapter.java",
			"""
				package test;
				
				final class DurationAdapter extends java.lang.ref.WeakReference<String> {
					public DurationAdapter(String referent) {
						super(referent);
					}
				}
				""",
			"test/TheAnnotation.java",
			"""
				package test;
				
				public @interface TheAnnotation {
					Class<? extends java.lang.ref.SoftReference<?>> value();
				}
				""",
			"test/package-info.java",
			"@TheAnnotation(value = DurationAdapter.class)\n" +
			"package test;\n",
		},
		"""
			----------
			1. ERROR in test\\package-info.java (at line 1)
				@TheAnnotation(value = DurationAdapter.class)
				                       ^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from Class<DurationAdapter> to Class<? extends SoftReference<?>>
			----------
			""",
		this.LIBS,
		true, /*flush*/
		getCompilerOptions()
	);
}
public void testBug502113() {
	runConformTestWithLibs(
		new String[] {
			"test/I.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public interface I {\n" +
			"	String method();\n" +
			"\n" +
			"	boolean equals(@Nullable Object obj);\n" +
			"	@NonNull String toString();\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"public class X implements I {\n" +
			"	public String method() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		""
	);
}
public void testBug502113b() {
	runNegativeTestWithLibs(
		new String[] {
			"test/I.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"\n" +
			"public interface I {\n" +
			"	String method();\n" +
			"\n" +
			"	boolean equals(@Nullable Object obj);\n" +
			"	@NonNull String toString();\n" +
			"}\n" +
			"",
			"test/X.java",
			"package test;\n" +
			"\n" +
			"public class X implements I {\n" +
			"	public String method() {\n" +
			"		return \"\";\n" +
			"	}\n" +
			"	@Override\n" +
			"	public boolean equals(Object other) {\n" +
			"		return false;\n" +
			"	}\n" +
			"	@Override\n" +
			"	public String toString() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in test\\X.java (at line 8)
				public boolean equals(Object other) {
				                      ^^^^^^
			Missing nullable annotation: inherited method from I specifies this parameter as @Nullable
			----------
			2. ERROR in test\\X.java (at line 12)
				public String toString() {
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from I.toString() (mismatching null constraints)
			----------
			"""
	);
}
public void testBug502214() {
	runNegativeTestWithLibs(
		new String[] {
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNull;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"class A {\n" +
			"	public boolean m1(Object obj) {\n" +
			"		return this == obj;\n" +
			"	}\n" +
			"	public @Nullable String m2() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"\n" +
			"interface I {\n" +
			"	public boolean m1(@Nullable Object obj);\n" +
			"	public @NonNull String m2(); \n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"	I f() {\n" +
			"		class Y extends A implements I {\n" +
			"		}\n" +
			"		return new Y();\n" +
			"	}\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		(this.complianceLevel < ClassFileConstants.JDK1_8 ?
		"""
			----------
			1. ERROR in test\\X.java (at line 22)
				class Y extends A implements I {
				                ^
			The method m2() from A cannot implement the corresponding method from I due to incompatible nullness constraints
			----------
			2. ERROR in test\\X.java (at line 22)
				class Y extends A implements I {
				                ^
			The method m1(Object) from A cannot implement the corresponding method from I due to incompatible nullness constraints
			----------
			"""
		:
		"""
			----------
			1. ERROR in test\\X.java (at line 22)
				class Y extends A implements I {
				                ^
			The method @Nullable String m2() from A cannot implement the corresponding method from I due to incompatible nullness constraints
			----------
			2. ERROR in test\\X.java (at line 22)
				class Y extends A implements I {
				                ^
			The method m1(Object) from A cannot implement the corresponding method from I due to incompatible nullness constraints
			----------
			"""
		)
	);
}

//apply null default to parameters:
public void testBug530970_param() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import annotation.*;
				@NonNullByDefault(DefaultLocation.PARAMETER)
				public class X {
					Number test1(Number in) {
						System.out.print(in.intValue()); // OK
						test1(null); // ERR
						return null; // OK
					}
					java.lang.Number test2(java.lang.Number in) {
						System.out.print(in.intValue()); // OK
						test2(null); // ERR
						return null; // OK
					}
					void redundant(@NonNullByDefault(DefaultLocation.PARAMETER) java.lang.Number in) { // WARNING
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 6)
				test1(null); // ERR
				      ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			2. ERROR in X.java (at line 11)
				test2(null); // ERR
				      ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			3. WARNING in X.java (at line 14)
				void redundant(@NonNullByDefault(DefaultLocation.PARAMETER) java.lang.Number in) { // WARNING
				               ^^^^^^^^^^^^^^^^^
			Nullness default is redundant with a default specified for the enclosing type X
			----------
			"""
	);
}

//apply null default to return type - annotation at method:
public void testBug530970_return() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import annotation.*;
				public class X {
					@NonNullByDefault(DefaultLocation.RETURN_TYPE)
					Number test(Number in) {
						System.out.print(in.intValue());
						test(null); // OK
						return null; // ERR
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 7)
				return null; // ERR
				       ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");
}

//apply null default to field
public void testBug530970_field() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import annotation.*;
				@NonNullByDefault(DefaultLocation.FIELD)
				public class X {
					Number field; // ERR since uninitialized
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 4)
				Number field; // ERR since uninitialized
				       ^^^^^
			The @NonNull field field may not have been initialized
			----------
			""");
}

//default default
public void testBug530970_default() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import annotation.*;
				@NonNullByDefault
				public class X {
					Number field; // ERR since uninitialized
					void test1(Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
					void test2(java.lang.Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in X.java (at line 4)
				Number field; // ERR since uninitialized
				       ^^^^^
			The @NonNull field field may not have been initialized
			----------
			""");
}

//apply null default to parameters:
public void testBug530970_param_bin() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"X.java",
			"""
				import annotation.*;
				@NonNullByDefault(DefaultLocation.PARAMETER)
				public class X {
					Number test1(Number in) {
						return null; // OK
					}
				}
				"""
		},
		customOptions,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"""
				import annotation.*;
				public class Y {
					@NonNull Number test(X x) {
						return x.test1(null); // error at arg, unchecked at return
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. WARNING in Y.java (at line 4)
				return x.test1(null); // error at arg, unchecked at return
				       ^^^^^^^^^^^^^
			Null type safety: The expression of type \'Number\' needs unchecked conversion to conform to \'@NonNull Number\'
			----------
			2. ERROR in Y.java (at line 4)
				return x.test1(null); // error at arg, unchecked at return
				               ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");
}

//apply null default to return type - annotation at method:
public void testBug530970_return_bin() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"X.java",
			"""
				import annotation.*;
				public class X {
					@NonNullByDefault(DefaultLocation.RETURN_TYPE)
					Number test(Number in) {
						return new MyInteger(13);
					}
				}
				class MyInteger extends Number {
				private static final long serialVersionUID = 1L;
					public MyInteger(int i) {}
					@Override
					public int intValue() {	return 0;}
					@Override
					public long longValue() { return 0;	}
					@Override
					public float floatValue() {	return 0;}
					@Override
					public double doubleValue() { return 0;	}
				}
				"""

		},
		customOptions,
		"");
	runConformTestWithLibs(
		false /* don't flush */,
		new String[] {
			"Y.java",
			"""
				import annotation.*;
				public class Y {
					@NonNull Number test(X x) {
						return x.test(null); // both OK
					}
				}
				"""
		},
		customOptions,
		"");
}

//apply null default to field
public void testBug530970_field_bin() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"X.java",
			"""
				import annotation.*;
				@NonNullByDefault(DefaultLocation.FIELD)
				public class X {
					Number field = new MyDouble(1.1);
				}
				class MyDouble extends Number {
				private static final long serialVersionUID = 1L;
					public MyDouble(double d) {}
					@Override
					public int intValue() {	return 0;}
					@Override
					public long longValue() { return 0;	}
					@Override
					public float floatValue() {	return 0;}
					@Override
					public double doubleValue() { return 0;	}
				}
				"""
		},
		customOptions,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"""
				public class Y {
					void test(X x) {
						x.field = null; // ERR
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in Y.java (at line 3)
				x.field = null; // ERR
				          ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");}

//default default
public void testBug530970_default_bin() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
		false,
		new String[] {
			"X.java",
			"""
				import annotation.*;
				@NonNullByDefault
				public class X {
					Number field = new MyLong(13);
					void test1(Number[] ns) {
						ns[0] = null; // OK since not affected by default
					}
				}
				class MyLong extends Number {
				private static final long serialVersionUID = 1L;
					public MyLong(long l) {}
					@Override
					public int intValue() {	return 0;}
					@Override
					public long longValue() { return 0;	}
					@Override
					public float floatValue() {	return 0;}
					@Override
					public double doubleValue() { return 0;	}
				}
				"""
		},
		customOptions,
		"");
	runNegativeTestWithLibs(
		new String[] {
			"Y.java",
			"""
				public class Y {
					void test(X x) {
						x.test1(new Number[1]); // OK since not affected by default
						x.field = null; // ERR
					}
				}
				"""
		},
		customOptions,
		"""
			----------
			1. ERROR in Y.java (at line 4)
				x.field = null; // ERR
				          ^^^^
			Null type mismatch: required \'@NonNull Number\' but the provided value is null
			----------
			""");}

public void testBug530970_on_field_and_local() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runNegativeTestWithLibs(
		new String[] {
			"nnbd_test1/Test.java",
			"package nnbd_test1;\n" +
			"\n" +
			"import annotation.DefaultLocation;\n" +
			"import annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"abstract class X {\n" +
			"    @NonNullByDefault(DefaultLocation.FIELD)\n" +
			"    public Object a = \"\";\n" +
			"\n" +
			"    @NonNullByDefault({})\n" +
			"    public Object b;\n" +
			"\n" +
			"    @NonNullByDefault\n" +
			"    abstract String f(Integer p);\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"    abstract String g(Integer p);\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"    abstract String h(Integer p);\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"    abstract String i(@NonNullByDefault({}) Integer p);\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"    @NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"    X x1 = new X() {\n" +
			"        @Override\n" +
			"        public String f(Integer p) { // warning on parameter expected\n" +
			"            this.a = null; // warning expected\n" +
			"            this.b = null;\n" +
			"            return null; // warning expected\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String g(Integer p) {\n" +
			"            return null; // warning expected\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String h(Integer p) { // warning on parameter type expected\n" +
			"            return null; // warning expected\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String i(Integer p) {\n" +
			"            return null; // warning expected\n" +
			"        }\n" +
			"    };\n" +
			"    @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"    X x2 = new X() {\n" +
			"        @Override\n" +
			"        public String f(Integer p) { // warning on return type expected\n" +
			"            this.a = null; // warning expected\n" +
			"            this.b = null;\n" +
			"            return null;\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String g(Integer p) { // warning on return type and parameter expected\n" +
			"            return null;\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String h(Integer p) {\n" +
			"            return null;\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String i(Integer p) { // warning on parameter expected\n" +
			"            return null;\n" +
			"        }\n" +
			"    };\n" +
			"\n" +
			"    void method() {\n" +
			"        @NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"        X l1 = new X() {\n" +
			"            @Override\n" +
			"            public String f(Integer p) { // warning on parameter expected\n" +
			"                this.a = null; // warning expected\n" +
			"                this.b = null;\n" +
			"                return null; // warning expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String g(Integer p) {\n" +
			"                return null; // warning expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String h(Integer p) { // warning on parameter type expected\n" +
			"                return null; // warning expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String i(Integer p) {\n" +
			"                return null; // warning expected\n" +
			"            }\n" +
			"        };\n" +
			"        @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"        X l2 = new X() {\n" +
			"            @Override\n" +
			"            public String f(Integer p) { // warning on return type expected\n" +
			"                this.a = null; // warning expected\n" +
			"                this.b = null;\n" +
			"                return null;\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String g(Integer p) { // warning on return type and parameter expected\n" +
			"                return null;\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String h(Integer p) {\n" +
			"                return null;\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String i(Integer p) { // warning on parameter expected\n" +
			"                return null;\n" +
			"            }\n" +
			"        };\n" +
			"\n" +
			"        l1.equals(l2);\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"""
			----------
			1. WARNING in nnbd_test1\\Test.java (at line 32)
				public String f(Integer p) { // warning on parameter expected
				                ^^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			2. ERROR in nnbd_test1\\Test.java (at line 33)
				this.a = null; // warning expected
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			3. ERROR in nnbd_test1\\Test.java (at line 35)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			4. ERROR in nnbd_test1\\Test.java (at line 40)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			5. WARNING in nnbd_test1\\Test.java (at line 44)
				public String h(Integer p) { // warning on parameter type expected
				                ^^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			6. ERROR in nnbd_test1\\Test.java (at line 45)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			7. ERROR in nnbd_test1\\Test.java (at line 50)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			8. ERROR in nnbd_test1\\Test.java (at line 56)
				public String f(Integer p) { // warning on return type expected
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from X.f(Integer) (mismatching null constraints)
			----------
			9. ERROR in nnbd_test1\\Test.java (at line 57)
				this.a = null; // warning expected
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			10. ERROR in nnbd_test1\\Test.java (at line 63)
				public String g(Integer p) { // warning on return type and parameter expected
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from X.g(Integer) (mismatching null constraints)
			----------
			11. ERROR in nnbd_test1\\Test.java (at line 63)
				public String g(Integer p) { // warning on return type and parameter expected
				                ^^^^^^^
			Illegal redefinition of parameter p, inherited method from X does not constrain this parameter
			----------
			12. ERROR in nnbd_test1\\Test.java (at line 73)
				public String i(Integer p) { // warning on parameter expected
				                ^^^^^^^
			Illegal redefinition of parameter p, inherited method from X does not constrain this parameter
			----------
			13. WARNING in nnbd_test1\\Test.java (at line 82)
				public String f(Integer p) { // warning on parameter expected
				                ^^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			14. ERROR in nnbd_test1\\Test.java (at line 83)
				this.a = null; // warning expected
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			15. ERROR in nnbd_test1\\Test.java (at line 85)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			16. ERROR in nnbd_test1\\Test.java (at line 90)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			17. WARNING in nnbd_test1\\Test.java (at line 94)
				public String h(Integer p) { // warning on parameter type expected
				                ^^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			18. ERROR in nnbd_test1\\Test.java (at line 95)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			19. ERROR in nnbd_test1\\Test.java (at line 100)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			20. ERROR in nnbd_test1\\Test.java (at line 106)
				public String f(Integer p) { // warning on return type expected
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from X.f(Integer) (mismatching null constraints)
			----------
			21. ERROR in nnbd_test1\\Test.java (at line 107)
				this.a = null; // warning expected
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			22. ERROR in nnbd_test1\\Test.java (at line 113)
				public String g(Integer p) { // warning on return type and parameter expected
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from X.g(Integer) (mismatching null constraints)
			----------
			23. ERROR in nnbd_test1\\Test.java (at line 113)
				public String g(Integer p) { // warning on return type and parameter expected
				                ^^^^^^^
			Illegal redefinition of parameter p, inherited method from X does not constrain this parameter
			----------
			24. ERROR in nnbd_test1\\Test.java (at line 123)
				public String i(Integer p) { // warning on parameter expected
				                ^^^^^^^
			Illegal redefinition of parameter p, inherited method from X does not constrain this parameter
			----------
			"""
	);
}
public void testBug530970_on_field_bin() {
	Map customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "annotation.Nullable");
	customOptions.put(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "annotation.NonNull");
	customOptions.put(JavaCore.COMPILER_NONNULL_BY_DEFAULT_ANNOTATION_NAME, "annotation.NonNullByDefault");
	customOptions.put(JavaCore.COMPILER_PB_DEAD_CODE, JavaCore.IGNORE);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	runConformTestWithLibs(
		new String[] {
			"annotation/DefaultLocation.java",
			"package annotation;\n" +
			"\n" +
			"public enum DefaultLocation {\n" +
			"    PARAMETER, RETURN_TYPE, FIELD\n" +
			"}\n" +
			"",
			"annotation/NonNull.java",
			"package annotation;\n" +
			"\n" +
			"public @interface NonNull {\n" +
			"}\n" +
			"",
			"annotation/NonNullByDefault.java",
			"package annotation;\n" +
			"\n" +
			"import static annotation.DefaultLocation.*;\n" +
			" \n" +
			"public @interface NonNullByDefault {\n" +
			"	DefaultLocation[] value() default { PARAMETER, RETURN_TYPE, FIELD };\n" +
			"}\n" +
			"",
			"annotation/Nullable.java",
			"package annotation;\n" +
			"\n" +
			"public @interface Nullable {\n" +
			"}\n" +
			"",
		},
		customOptions,
		""
	);
	runConformTestWithLibs(
			false,
			new String[] {
				"nnbd_test1/X.java",
				"package nnbd_test1;\n" +
				"\n" +
				"import annotation.DefaultLocation;\n" +
				"import annotation.NonNullByDefault;\n" +
				"\n" +
				"@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
				"abstract class X {\n" +
				"    @NonNullByDefault(DefaultLocation.FIELD)\n" +
				"    public Object a = \"\";\n" +
				"\n" +
				"    @NonNullByDefault({})\n" +
				"    public Object b;\n" +
				"\n" +
				"    @NonNullByDefault\n" +
				"    abstract String f(Integer p);\n" +
				"\n" +
				"    @NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
				"    abstract String g(Integer p);\n" +
				"\n" +
				"    @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
				"    abstract String h(Integer p);\n" +
				"\n" +
				"    @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
				"    abstract String i(@NonNullByDefault({}) Integer p);\n" +
				"}\n" +
				"",
			},
			customOptions,
			""
		);
	runNegativeTestWithLibs(
		new String[] {
			"nnbd_test1/Test.java",
			"package nnbd_test1;\n" +
			"\n" +
			"import annotation.DefaultLocation;\n" +
			"import annotation.NonNullByDefault;\n" +
			"\n" +
			"@NonNullByDefault({ DefaultLocation.RETURN_TYPE, DefaultLocation.PARAMETER })\n" +
			"abstract class Unused { // just here to keep line number in sync with testBug530970_on_field_and_local\n" +
			"    @NonNullByDefault(DefaultLocation.FIELD)\n" +
			"    public Object a = \"\";\n" +
			"\n" +
			"    @NonNullByDefault({})\n" +
			"    public Object b;\n" +
			"\n" +
			"    @NonNullByDefault\n" +
			"    abstract String f(Integer p);\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"    abstract String g(Integer p);\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"    abstract String h(Integer p);\n" +
			"\n" +
			"    @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"    abstract String i(@NonNullByDefault({}) Integer p);\n" +
			"}\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class Test {\n" +
			"    @NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"    X x1 = new X() {\n" +
			"        @Override\n" +
			"        public String f(Integer p) { // warning on parameter expected\n" +
			"            this.a = null; // warning expected\n" +
			"            this.b = null;\n" +
			"            return null; // warning expected\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String g(Integer p) {\n" +
			"            return null; // warning expected\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String h(Integer p) { // warning on parameter type expected\n" +
			"            return null; // warning expected\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String i(Integer p) {\n" +
			"            return null; // warning expected\n" +
			"        }\n" +
			"    };\n" +
			"    @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"    X x2 = new X() {\n" +
			"        @Override\n" +
			"        public String f(Integer p) { // warning on return type expected\n" +
			"            this.a = null; // warning expected\n" +
			"            this.b = null;\n" +
			"            return null;\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String g(Integer p) { // warning on return type and parameter expected\n" +
			"            return null;\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String h(Integer p) {\n" +
			"            return null;\n" +
			"        }\n" +
			"\n" +
			"        @Override\n" +
			"        public String i(Integer p) { // warning on parameter expected\n" +
			"            return null;\n" +
			"        }\n" +
			"    };\n" +
			"\n" +
			"    void method() {\n" +
			"        @NonNullByDefault(DefaultLocation.RETURN_TYPE)\n" +
			"        X l1 = new X() {\n" +
			"            @Override\n" +
			"            public String f(Integer p) { // warning on parameter expected\n" +
			"                this.a = null; // warning expected\n" +
			"                this.b = null;\n" +
			"                return null; // warning expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String g(Integer p) {\n" +
			"                return null; // warning expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String h(Integer p) { // warning on parameter type expected\n" +
			"                return null; // warning expected\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String i(Integer p) {\n" +
			"                return null; // warning expected\n" +
			"            }\n" +
			"        };\n" +
			"        @NonNullByDefault(DefaultLocation.PARAMETER)\n" +
			"        X l2 = new X() {\n" +
			"            @Override\n" +
			"            public String f(Integer p) { // warning on return type expected\n" +
			"                this.a = null; // warning expected\n" +
			"                this.b = null;\n" +
			"                return null;\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String g(Integer p) { // warning on return type and parameter expected\n" +
			"                return null;\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String h(Integer p) {\n" +
			"                return null;\n" +
			"            }\n" +
			"\n" +
			"            @Override\n" +
			"            public String i(Integer p) { // warning on parameter expected\n" +
			"                return null;\n" +
			"            }\n" +
			"        };\n" +
			"\n" +
			"        l1.equals(l2);\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		customOptions,
		"""
			----------
			1. WARNING in nnbd_test1\\Test.java (at line 32)
				public String f(Integer p) { // warning on parameter expected
				                ^^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			2. ERROR in nnbd_test1\\Test.java (at line 33)
				this.a = null; // warning expected
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			3. ERROR in nnbd_test1\\Test.java (at line 35)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			4. ERROR in nnbd_test1\\Test.java (at line 40)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			5. WARNING in nnbd_test1\\Test.java (at line 44)
				public String h(Integer p) { // warning on parameter type expected
				                ^^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			6. ERROR in nnbd_test1\\Test.java (at line 45)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			7. ERROR in nnbd_test1\\Test.java (at line 50)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			8. ERROR in nnbd_test1\\Test.java (at line 56)
				public String f(Integer p) { // warning on return type expected
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from X.f(Integer) (mismatching null constraints)
			----------
			9. ERROR in nnbd_test1\\Test.java (at line 57)
				this.a = null; // warning expected
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			10. ERROR in nnbd_test1\\Test.java (at line 63)
				public String g(Integer p) { // warning on return type and parameter expected
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from X.g(Integer) (mismatching null constraints)
			----------
			11. ERROR in nnbd_test1\\Test.java (at line 63)
				public String g(Integer p) { // warning on return type and parameter expected
				                ^^^^^^^
			Illegal redefinition of parameter p, inherited method from X does not constrain this parameter
			----------
			12. ERROR in nnbd_test1\\Test.java (at line 73)
				public String i(Integer p) { // warning on parameter expected
				                ^^^^^^^
			Illegal redefinition of parameter p, inherited method from X does not constrain this parameter
			----------
			13. WARNING in nnbd_test1\\Test.java (at line 82)
				public String f(Integer p) { // warning on parameter expected
				                ^^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			14. ERROR in nnbd_test1\\Test.java (at line 83)
				this.a = null; // warning expected
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			15. ERROR in nnbd_test1\\Test.java (at line 85)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			16. ERROR in nnbd_test1\\Test.java (at line 90)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			17. WARNING in nnbd_test1\\Test.java (at line 94)
				public String h(Integer p) { // warning on parameter type expected
				                ^^^^^^^
			Missing non-null annotation: inherited method from X specifies this parameter as @NonNull
			----------
			18. ERROR in nnbd_test1\\Test.java (at line 95)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			19. ERROR in nnbd_test1\\Test.java (at line 100)
				return null; // warning expected
				       ^^^^
			Null type mismatch: required \'@NonNull String\' but the provided value is null
			----------
			20. ERROR in nnbd_test1\\Test.java (at line 106)
				public String f(Integer p) { // warning on return type expected
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from X.f(Integer) (mismatching null constraints)
			----------
			21. ERROR in nnbd_test1\\Test.java (at line 107)
				this.a = null; // warning expected
				         ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			22. ERROR in nnbd_test1\\Test.java (at line 113)
				public String g(Integer p) { // warning on return type and parameter expected
				       ^^^^^^
			The return type is incompatible with \'@NonNull String\' returned from X.g(Integer) (mismatching null constraints)
			----------
			23. ERROR in nnbd_test1\\Test.java (at line 113)
				public String g(Integer p) { // warning on return type and parameter expected
				                ^^^^^^^
			Illegal redefinition of parameter p, inherited method from X does not constrain this parameter
			----------
			24. ERROR in nnbd_test1\\Test.java (at line 123)
				public String i(Integer p) { // warning on parameter expected
				                ^^^^^^^
			Illegal redefinition of parameter p, inherited method from X does not constrain this parameter
			----------
			"""
	);
}
public void testBug542707_001() {
	if (this.complianceLevel < ClassFileConstants.JDK12)
		return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_12);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_12);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_12);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runNegativeTestWithLibs(
			new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				import org.eclipse.jdt.annotation.NonNull;
				
				public class X {
					public static int foo(int i) throws IOException {
						int k = 0;
						@NonNull
						X x = new X();
						x  = switch (i) {\s
						case 1  ->   {
							x = null;
							break x;
						}
						default -> null;
						};
				
						return k ;
					}
				
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// do nothing
						}
					}
				}
				"""
				},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 0)\n" +
		"	import java.io.IOException;\n" +
		"	^\n" +
		"Preview features enabled at an invalid source release level 12, preview can be enabled only at source level "+AbstractRegressionTest.PREVIEW_ALLOWED_LEVEL+"\n" +
		"----------\n"
	);
}
/**
 * should not throw IOOBE while building - a safety check test case.
 */
public void testBug542707_002() {
	if (this.complianceLevel != ClassFileConstants.JDK12)
		return;
	Map options = getCompilerOptions();
	options.put(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_12);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_12);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_12);
	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runNegativeTestWithLibs(
			new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    	void m1(@NonNull String a) {}
						void m2(@Nullable String b, int i) {
							m1(switch(i) {
							case 0 : {
								break "hello";
							}
							default : break "world";
							});
						}
						void m3() {
							Zork();
						}
				}
				"""
				},
		options,
		"----------\n" +
		"1. ERROR in X.java (at line 0)\n" +
		"	import org.eclipse.jdt.annotation.*;\n" +
		"	^\n" +
		"Preview features enabled at an invalid source release level 12, preview can be enabled only at source level "+AbstractRegressionTest.PREVIEW_ALLOWED_LEVEL+"\n" +
		"----------\n"
	);
}
public void testBug542707_003() {
	if (this.complianceLevel < ClassFileConstants.JDK12) return; // switch expression
	// outer expected type (from assignment) is propagated deeply into a switch expression
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles = new String[] {
		"X.java",
		"""
			import org.eclipse.jdt.annotation.*;
			public class X {
				@Nullable String maybe() { return null; }
				void test(int i) {
					@NonNull String s = switch (i) {
						case 1 -> "";
						default -> i == 3 ? maybe() : "";
					};
					System.out.println(s.toLowerCase());
				}
			}
			"""
	};
	runner.expectedCompilerLog = checkPreviewAllowed() ?
			"""
				----------
				1. ERROR in X.java (at line 7)
					default -> i == 3 ? maybe() : "";
					                    ^^^^^^^
				Null type mismatch (type annotations): required \'@NonNull String\' but this expression has type \'@Nullable String\'
				----------
				""" :
			"----------\n" +
			"1. ERROR in X.java (at line 0)\n" +
			"	import org.eclipse.jdt.annotation.*;\n" +
			"	^\n" +
			"Preview features enabled at an invalid source release level "+CompilerOptions.versionFromJdkLevel(this.complianceLevel)+", preview can be enabled only at source level "+AbstractRegressionTest.PREVIEW_ALLOWED_LEVEL+"\n" +
			"----------\n";
	runner.runNegativeTest();
}
// failing, see https://bugs.eclipse.org/543860
public void _testBug542707_004() {
	if (this.complianceLevel < ClassFileConstants.JDK12) return; // switch expression
	// outer expected type (from method parameter) is propagated deeply into a switch expression
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles = new String[] {
		"X.java",
		"""
			import org.eclipse.jdt.annotation.*;
			public class X {
				@Nullable String maybe() { return null; }
				void need(@NonNull String s) {
					System.out.println(s.toLowerCase());
				}
				void test(int i) {
					need(switch (i) {
						case 1 -> "";
						default -> i == 3 ? maybe() : "";
					});
				}
			}
			"""
	};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in X.java (at line 10)
					default -> i == 3 ? maybe() : "";
					                    ^^^^^^^
				Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'
				----------
				""";
	runner.runNegativeTest();
}
public void testBug542707_005() {
	if (this.complianceLevel < ClassFileConstants.JDK12) return; // switch expression
	// switch value must not be null (@Nullable)
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles = new String[] {
		"X.java",
		"""
			import org.eclipse.jdt.annotation.*;
			enum SomeDays { Mon, Wed, Fri }
			public class X {
				int testEnum(@Nullable SomeDays day) {
					return switch(day) {
					case Mon -> 1;
					case Wed -> 2;
					case Fri -> 3;
					};
				}
			}
			"""
	};
	runner.expectedCompilerLog = checkPreviewAllowed() ?
			"""
				----------
				1. ERROR in X.java (at line 5)
					return switch(day) {
					              ^^^
				Potential null pointer access: this expression has a \'@Nullable\' type
				----------
				""" :
			"----------\n" +
			"1. ERROR in X.java (at line 0)\n" +
			"	import org.eclipse.jdt.annotation.*;\n" +
			"	^\n" +
			"Preview features enabled at an invalid source release level "+CompilerOptions.versionFromJdkLevel(this.complianceLevel)+", preview can be enabled only at source level "+AbstractRegressionTest.PREVIEW_ALLOWED_LEVEL+"\n" +
			"----------\n";
	runner.runNegativeTest();
}
public void testBug542707_006() {
	if (this.complianceLevel < ClassFileConstants.JDK12) return; // switch expression
	// switch value must not be null (pot-null by flow analysis)
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	runner.customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runner.classLibraries = this.LIBS;
	runner.testFiles = new String[] {
		"X.java",
		"""
			enum SomeDays { Mon, Wed, Fri }
			public class X {
				int testEnum(boolean b) {
					SomeDays day = b ? SomeDays.Mon : null;
					return switch(day) {
					case Mon -> 1;
					case Wed -> 2;
					case Fri -> 3;
					};
				}
			}
			"""
	};
	runner.expectedCompilerLog = checkPreviewAllowed() ?
			"""
				----------
				2. ERROR in X.java (at line 5)
					return switch(day) {
					              ^^^
				Potential null pointer access: The variable day may be null at this location
				----------
				""" :
			"----------\n" +
			"1. ERROR in X.java (at line 0)\n" +
			"	enum SomeDays { Mon, Wed, Fri }\n" +
			"	^\n" +
			"Preview features enabled at an invalid source release level "+CompilerOptions.versionFromJdkLevel(this.complianceLevel)+", preview can be enabled only at source level "+AbstractRegressionTest.PREVIEW_ALLOWED_LEVEL+"\n" +
			"----------\n";
	runner.runNegativeTest();
}
public void testBug545715() {
	if (!checkPreviewAllowed()) return; // switch expression
	Map<String, String>  customOptions = getCompilerOptions();
	customOptions.put(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    void f() {
				        loop: while(true) {
				            break loop;
				        }
				    }
				    public static void main(String[] args) {
				        new X().f();
				    }
				}
				"""
		},
	    "",
	    customOptions,
	    new String[] {"--enable-preview"});
}
public void testBug548418_001a() {
	if (this.complianceLevel < ClassFileConstants.JDK14) return;
	runNegativeTestWithLibs(
			new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				import org.eclipse.jdt.annotation.NonNull;
				
				public class X {
					public static int foo(int i) throws IOException {
						int k = 0;
						@NonNull
						X x = new X();
						x  = switch (i) {\s
						case 1  ->   {
							x = null;
							break x;
						}
						default -> null;
						};
				
						return k ;
					}
				
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// do nothing
						}
					}
				}
				"""
				},
			"""
				----------
				1. ERROR in X.java (at line 12)
					x = null;
					    ^^^^
				Null type mismatch: required '@NonNull X' but the provided value is null
				----------
				2. ERROR in X.java (at line 13)
					break x;
					^^^^^^^^
				Breaking out of switch expressions not permitted
				----------
				3. ERROR in X.java (at line 15)
					default -> null;
					           ^^^^
				Null type mismatch: required '@NonNull X' but the provided value is null
				----------
				""");
}
public void testBug548418_001b() {
	if (this.complianceLevel < ClassFileConstants.JDK14) return;
	runNegativeTestWithLibs(
			new String[] {
			"X.java",
			"""
				import java.io.IOException;
				
				import org.eclipse.jdt.annotation.NonNull;
				
				public class X {
					public static int foo(int i) throws IOException {
						int k = 0;
						@NonNull
						X x = new X();
						x  = switch (i) {\s
						case 1  ->   {
							x = null;
							yield x;
						}
						default -> null;
						};
				
						return k ;
					}
				
					public static void main(String[] args) {
						try {
							System.out.println(foo(3));
						} catch (IOException e) {
							// do nothing
						}
					}
				}
				"""
				},
		"""
			----------
			1. ERROR in X.java (at line 12)
				x = null;
				    ^^^^
			Null type mismatch: required \'@NonNull X\' but the provided value is null
			----------
			2. ERROR in X.java (at line 15)
				default -> null;
				           ^^^^
			Null type mismatch: required \'@NonNull X\' but the provided value is null
			----------
			"""
	);
}
public void testBug548418_002a() {
	if (this.complianceLevel < ClassFileConstants.JDK14) return;
	runNegativeTestWithLibs(
			new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    	void m1(@NonNull String a) {}
						void m2(@Nullable String b, int i) {
							m1(switch(i) {
							case 0 : {
								break "hello";
							}
							default : break "world";
							});
						}
						void m3() {
							Zork();
						}
				}
				"""
				},
		"""
			----------
			1. ERROR in X.java (at line 7)
				break "hello";
				      ^^^^^^^
			Syntax error on token ""hello"", delete this token
			----------
			2. ERROR in X.java (at line 9)
				default : break "world";
				                ^^^^^^^
			Syntax error on token ""world"", delete this token
			----------
			3. ERROR in X.java (at line 13)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			"""
	);
}
public void testBug548418_002b() {
	if (this.complianceLevel < ClassFileConstants.JDK14) return;
	runNegativeTestWithLibs(
			new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
				    	void m1(@NonNull String a) {}
						void m2(@Nullable String b, int i) {
							m1(switch(i) {
							case 0 : {
								yield "hello";
							}
							default : yield "world";
							});
						}
						void m3() {
							Zork();
						}
				}
				"""
				},
		"""
			----------
			1. ERROR in X.java (at line 13)
				Zork();
				^^^^
			The method Zork() is undefined for the type X
			----------
			"""
			);
}
public void testBug499714() {
	runConformTestWithLibs(
		new String[] {
			"test/X.java",
			"package test;\n" +
			"\n" +
			"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
			"import org.eclipse.jdt.annotation.Nullable;\n" +
			"\n" +
			"@NonNullByDefault\n" +
			"public class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        Object o = null;\n" +
			"        for (final String s : args) {\n" +
			"            if (s.equals(\"-x\")) {\n" +
			"                if (o != null) { // bogus warning here\n" +
			"                    //\n" +
			"                }\n" +
			"                continue;\n" +
			"            }\n" +
			"            o = read();\n" +
			"        }\n" +
			"    }\n" +
			"\n" +
			"    @Nullable\n" +
			"    public static Object read() {\n" +
			"        return \"\";\n" +
			"    }\n" +
			"}\n" +
			"",
		},
		getCompilerOptions(),
		"");
}
public void testBug481931_source() {
	runNegativeTestWithLibs(
		new String[] {
			"X.java",
			"""
				import org.eclipse.jdt.annotation.*;
				public class X {
					static final String CONST = "const1";
					final String INST_CONST = "const2" + CONST;
					@NonNull String getInstConst() {
						if (INST_CONST == null) {
							System.out.println("null");
						}
						return INST_CONST;
					}
					static @NonNull String getConst() {
						if (CONST == null) {
							System.out.println("null");
						}
						return CONST;
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in X.java (at line 6)
				if (INST_CONST == null) {
				    ^^^^^^^^^^
			Null comparison always yields false: The field INST_CONST is a nonnull constant
			----------
			2. WARNING in X.java (at line 6)
				if (INST_CONST == null) {
						System.out.println("null");
					}
				                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			3. ERROR in X.java (at line 12)
				if (CONST == null) {
				    ^^^^^
			Null comparison always yields false: The field CONST is a nonnull constant
			----------
			4. WARNING in X.java (at line 12)
				if (CONST == null) {
						System.out.println("null");
					}
				                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}
public void testBug481931_binary() {
	runConformTestWithLibs(
		new String[] {
			"test/X.java",
			"""
				package test;
				public class X {
					public static final String CONST = "const1";
					public final String INST_CONST = "const2" + CONST;
					X() {}
					X(int i) {}
				}
				"""
		},
		getCompilerOptions(),
		"");
	Runner runner = new Runner();
	runner.shouldFlushOutputDirectory = false;
	runner.testFiles = new String[] {
			"Y.java",
			"""
				import org.eclipse.jdt.annotation.*;
				import test.X;
				public class Y {
					@NonNull String getInstConst(X x) {
						if (x.INST_CONST == null) {
							System.out.println("null");
						}
						return x.INST_CONST;
					}
					static @NonNull String getConst() {
						if (X.CONST == null) {
							System.out.println("null");
						}
						return X.CONST;
					}
				}
				"""
		};
	runner.classLibraries = this.LIBS;
	runner.customOptions = getCompilerOptions();
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in Y.java (at line 5)
					if (x.INST_CONST == null) {
					      ^^^^^^^^^^
				Null comparison always yields false: The field INST_CONST is a nonnull constant
				----------
				2. WARNING in Y.java (at line 5)
					if (x.INST_CONST == null) {
							System.out.println("null");
						}
					                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				3. ERROR in Y.java (at line 11)
					if (X.CONST == null) {
					      ^^^^^
				Null comparison always yields false: The field CONST is a nonnull constant
				----------
				4. WARNING in Y.java (at line 11)
					if (X.CONST == null) {
							System.out.println("null");
						}
					                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Dead code
				----------
				""";
	runner.javacTestOptions = Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug459397() {
	runNegativeTestWithLibs(
		new String[] {
			"NonNullBug.java",
			"""
				import org.eclipse.jdt.annotation.NonNull;
				import org.eclipse.jdt.annotation.Nullable;
				
				public class NonNullBug {
					public static final String PACKid_$metamodel$ = null;
					public static final String PACKid_http_c_s_s_www_eclipse_org_s_qvt_s_examples_s_0_1_s_SimpleRDBMS = null;
					public static final String PACKid_http_c_s_s_www_eclipse_org_s_qvt_s_examples_s_0_1_s_UMLtoRDBMS = null;
					public static final String PACKid_http_c_s_s_www_eclipse_org_s_qvt_s_examples_s_0_1_s_simpleUML = null;
					public static final String CLSSid_Association = null;
					public static final String CLSSid_AssociationToForeignKey = null;
					public static final String CLSSid_Attribute = null;
					public static final String CLSSid_AttributeToColumn = null;
					public static final String CLSSid_Class = null;
					public static final String CLSSid_ClassToTable = null;
					public static final String CLSSid_Class_0 = null;
					public static final String CLSSid_Classifier = null;
					public static final String CLSSid_Column = null;
					public static final String CLSSid_ForeignKey = null;
					public static final String CLSSid_FromAttribute = null;
					public static final String CLSSid_Key = null;
					public static final String CLSSid_NonLeafAttribute = null;
					public static final String CLSSid_Package = null;
					public static final String CLSSid_PackageElement = null;
					public static final String CLSSid_PackageToSchema = null;
					public static final String CLSSid_PrimitiveDataType = null;
					public static final String CLSSid_PrimitiveToName = null;
					public static final String CLSSid_Schema = null;
					public static final String CLSSid_Table = null;
					public static final String STR_2 = "2";
					public static final String STR_BOOLEAN = "BOOLEAN";
					public static final String STR_Boolean = "Boolean";
					public static final String STR_Integer = "Integer";
					public static final String STR_NUMBER = "NUMBER";
					public static final String STR_String = "String";
					public static final String STR_VARCHAR = "VARCHAR";
					public static final String STR__ = "_";
					public static final String STR__pk = "_pk";
					public static final String STR__tid = "_tid";
					public static final String STR_base = "base";
					public static final String STR_persistent = "persistent";
					public static final String STR_primary = "primary";
					public static final String BAG_CLSSid_AttributeToColumn = null;
					public static final String BAG_CLSSid_FromAttribute = null;
					public static final String ORD_CLSSid_AssociationToForeignKey = null;
					public static final String ORD_CLSSid_Attribute = null;
					public static final String ORD_CLSSid_Column = null;
					public static final String ORD_CLSSid_ForeignKey = null;
					public static final String ORD_CLSSid_Key = null;
					public static final String ORD_CLSSid_PackageElement = null;
					public static final String SET_CLSSid_Association = null;
					public static final String SET_CLSSid_Attribute = null;
					public static final String SET_CLSSid_AttributeToColumn = null;
					public static final String SET_CLSSid_Class = null;
					public static final String SET_CLSSid_ClassToTable = null;
					public static final String SET_CLSSid_FromAttribute = null;
					public static final String SET_CLSSid_NonLeafAttribute = null;
					public static final String SET_CLSSid_Package = null;
					public static final String SET_CLSSid_PrimitiveToName = null;
				
					protected final String OPPOSITE_OF_ClassToTable_table = null;
					protected final String OPPOSITE_OF_ClassToTable_umlClass = null;
					protected final String OPPOSITE_OF_FromAttribute_attribute = null;
					protected final String OPPOSITE_OF_PrimitiveToName_primitive = null;
				
					@SuppressWarnings("unused")
					private static final String[] classIndex2classId = new String[] {};
					@SuppressWarnings("unused")
					private final static int[][] classIndex2allClassIndexes = new int[][] {};
				
					protected String x(final @NonNull Exception a, final @NonNull String p_4, final @Nullable String p2s_9) throws Exception {
						final @Nullable Throwable destination = a.getCause();
						final @Nullable Throwable dc = a.getCause();
						if (dc == null) {
							throw new Exception();
						}
						try {
							if (dc instanceof Exception) {
								throw (Exception) dc;
							}
							boolean eq_2 = (destination != null) ? destination.equals(dc) : (dc == null);
						} catch (Exception e) {
						}
						boolean ne = (destination != null) ? !destination.equals(dc) : (dc != null);
						return dc.toString();
					}
				}
				"""
		},
		getCompilerOptions(),
		"""
			----------
			1. ERROR in NonNullBug.java (at line 80)
				boolean eq_2 = (destination != null) ? destination.equals(dc) : (dc == null);
				                                                                 ^^
			Null comparison always yields false: The variable dc cannot be null at this location
			----------
			2. ERROR in NonNullBug.java (at line 83)
				boolean ne = (destination != null) ? !destination.equals(dc) : (dc != null);
				                                                                ^^
			Redundant null check: The variable dc cannot be null at this location
			----------
			""");
}
public void testBug466477() {
	runNegativeTestWithLibs(
		new String[] {
			"SuperI.java",
			"""
				import org.eclipse.jdt.annotation.*;
				
				public interface SuperI {
				  void testNN(@NonNull String s);
				  void testNu(@Nullable String s);
				}
				""",
			"Base.java",
			"""
				public class Base {
				  public void testNN(String s) { }
				  public void testNu(String s) { }
				}
				""",
			"Custom.java",
			"public class Custom extends Base implements SuperI {\n" +
			"}"
		},
		"""
			----------
			1. ERROR in Custom.java (at line 1)
				public class Custom extends Base implements SuperI {
				                            ^^^^
			The method testNu(String) from Base cannot implement the corresponding method from SuperI due to incompatible nullness constraints
			----------
			2. WARNING in Custom.java (at line 1)
				public class Custom extends Base implements SuperI {
				                            ^^^^
			Parameter 1 of method testNN(String) lacks a @NonNull annotation as specified in type SuperI
			----------
			""");
}
public void testBug565246() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"java/util/Iterator.java",
			"""
				package java.util;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				
				@NonNullByDefault
				public interface Iterator<E> {
					boolean hasNext();
				
					E next();
				}""",
			"bug/B.java",
			"""
				package bug;
				
				import java.util.Iterator;
				
				import org.eclipse.jdt.annotation.*;
				
				@NonNullByDefault
				public class B<E> extends A<E> {
				
					public void barKOWithForLoop(I<? extends E> c) {
						for (E e : c) {
							foo(e); //<-- Null type safety: The expression of type 'E' needs unchecked conversion to conform to '@NonNull E'
						}
					}
				
					public void barOKWithWhileIteratorLoop(I<? extends E> c) {
						Iterator<? extends E> it = c.iterator();
						while (it.hasNext()) {
							E e = it.next(); // <-- OK
							foo(e);
						}
					}
				
					@Override
					public void foo(E e) { }
				}
				
				@NonNullByDefault
				abstract class A<E> implements I<E> {
				
					@Nullable public E e;
				
					public Iterator<E> iterator() {
						return new Iterator<E>() {
							public boolean hasNext() {
								return false;
							}
							public E next() {
								E e = A.this.e;
								assert e != null;
								return e;
							}
						};
					}
				
					public void foo(E e) {
						throw new RuntimeException();
					}
				}
				
				@NonNullByDefault
				interface I<E> extends Iterable<E> {
					public Iterator<E> iterator();
					public void foo(E e);
				}
				"""
		};
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNullUncheckedConversion, CompilerOptions.ERROR);
	runner.classLibraries = this.LIBS;
	if (this.complianceLevel >= ClassFileConstants.JDK1_8) {
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in bug\\B.java (at line 39)
					E e = A.this.e;
					      ^^^^^^^^
				Null type mismatch (type annotations): required \'E\' but this expression has type \'@Nullable E\', where \'E\' is a free type variable
				----------
				""";
		runner.runNegativeTest();
	} else {
		runner.runConformTest();
	}
}
}
