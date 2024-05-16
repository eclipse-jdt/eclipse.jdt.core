/*******************************************************************************
 * Copyright (c) 2018, 2023 Jesper Steen Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.junit.Assert;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JEP286Test extends AbstractRegressionTest {

public static Class testClass() {
	return JEP286Test.class;
}
@Override
public void initialize(CompilerTestSetup setUp) {
	super.initialize(setUp);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_10);
}

public JEP286Test(String testName){
	super(testName);
}
static {
//	TESTS_NAMES = new String[] { "test0018_project_variable_types" };
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_10);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_10);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_10);
	return options;
}
private static final Map<String, String> simpleTypeNames = new HashMap<>();
static {
	// Below call forces the init when the test is run independently.
	AbstractCompilerTest.getPossibleComplianceLevels();
	simpleTypeNames.put("String", "java.lang.String");
	simpleTypeNames.put("Object", "java.lang.Object");
	simpleTypeNames.put("Bar", "X.Bar");

	simpleTypeNames.put("AnonymousObjectSubclass", "new java.lang.Object(){}");
	simpleTypeNames.put("AnonymousRunnableSubclass", "new java.lang.Runnable(){}");
	simpleTypeNames.put("CollectionOfExtString", "Collection<? extends java.lang.String>");
	simpleTypeNames.put("CollectionOfSuperString", "Collection<? super java.lang.String>");
	simpleTypeNames.put("CollectionAny", "Collection<?>");
	simpleTypeNames.put("ComparableAny", "Comparable<?>");
	simpleTypeNames.put("CollectionExt_ComparableAny", "Collection<? extends Comparable<?>>");
	simpleTypeNames.put("CollectionSuperComparableAny", "Collection<? super Comparable<?>>");
	if (isJRE12Plus)
		simpleTypeNames.put("IntLongFloat", "java.lang.Number & Comparable<?> & java.lang.constant.Constable & java.lang.constant.ConstantDesc");
	else
		simpleTypeNames.put("IntLongFloat", "java.lang.Number & Comparable<?>");
	simpleTypeNames.put("ListTestAndSerializable", "List<? extends Z & java.io.Serializable>");
	simpleTypeNames.put("TestAndSerializable", "Z & java.io.Serializable");
}

static void assertInferredType(LocalDeclaration varDecl) {
	String varName = new String(varDecl.name);
	int underscoreIndex = varName.indexOf('_');
	Assert.assertNotEquals(-1, underscoreIndex);
	String typeNamePart = varName.substring(underscoreIndex+1);
	typeNamePart = typeNamePart.replaceAll("ARRAY", "[]"); // So that we assume that x_intARRAY is of type int[]
	String expectedTypeName = simpleTypeNames.getOrDefault(typeNamePart, typeNamePart);
	String actualTypeName = varDecl.binding.type.debugName();
	// System.out.println("For " + varName + ", we expect " + expectedTypeName + ", the type was: "
	// + actualTypeName + " - " + (expectedTypeName.equals(actualTypeName) ? "❤️" : "🤡"));
	Assert.assertEquals("Type of variable " + varName, expectedTypeName, actualTypeName);
}

// This visitor visits the 'testXxx' method in the visited classes, checking for expected types of local variables (using the debug name)
private final static class InferredTypeVerifier extends ASTVisitor {
		public int localsChecked = 0;
		public InferredTypeVerifier() { }

		@Override
		public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
			return false; // Don't check Foo itself
		}

		@Override
		public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
			if (! new String(methodDeclaration.selector).startsWith("test")) return false;
			return super.visit(methodDeclaration, scope);
		}

		@Override
		public boolean visit(LocalDeclaration localDeclaration, BlockScope scope) {
			assertInferredType(localDeclaration);
			this.localsChecked++;
			return super.visit(localDeclaration, scope);
		}
	}

public void test0001_local_variable_inference() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					        var x = "SUCCESS";
					        System.out.println(x);
					    }
					}
					"""
			},
			"SUCCESS");
}
public void test0002_inferred_for() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
							int sum = 0;
							for(var n = 1; n <= 2; ++n) {
								sum += n;
					       }
							System.out.println("SUCCESS " + sum);
					    }
					}
					"""
			},
			"SUCCESS 3");
}
public void test0003_inferred_enhanced_for() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
							int sum = 0;
							for(var n : java.util.List.of(1, 2)) {
								sum += n;
					       }
							System.out.println("SUCCESS " + sum);
					    }
					}
					"""
			},
			"SUCCESS 3");
}
public void test0004_try_with_resources() throws IOException {
	try(java.io.Writer w = new java.io.StringWriter()) {
		w.write("SUCCESS!\n");
		System.out.println(w.toString());
	}

	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) throws Exception {
							try(var w = new java.io.StringWriter()) {
								w.write("SUCCESS\\n");\
								System.out.println(w.toString());
					       }
					    }
					}
					"""
			},
			"SUCCESS");
}
public void test0005_no_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] argv) {
							var a;
							for(var b;;);
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var a;
					    ^
				Cannot use 'var' on variable without initializer
				----------
				2. ERROR in X.java (at line 4)
					for(var b;;);
					        ^
				Cannot use 'var' on variable without initializer
				----------
				""");
}
public void test0006_multiple_declarators() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] argv) {
							var a = 1, b = 2;
							for(var c = 1, d = 20; c<d; c++);
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var a = 1, b = 2;
					           ^
				'var' is not allowed in a compound declaration
				----------
				2. ERROR in X.java (at line 4)
					for(var c = 1, d = 20; c<d; c++);
					               ^
				'var' is not allowed in a compound declaration
				----------
				""");
}
public void test0007_var_in_wrong_place() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						private var someField = 0;
						public var method() {
							return null;
						}
						public void main(var arg) {
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					private var someField = 0;
					        ^^^
				'var' is not allowed here
				----------
				2. ERROR in X.java (at line 3)
					public var method() {
					       ^^^
				'var' is not allowed here
				----------
				3. ERROR in X.java (at line 6)
					public void main(var arg) {
					                 ^^^
				'var' is not allowed here
				----------
				""");
}
public void test0008_null_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void main(String[] arg) {
							var notMuch = null;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var notMuch = null;
					    ^^^^^^^
				Cannot infer type for local variable initialized to 'null'
				----------
				""");
}
public void test0008_void_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo() {
						}
					
						public void baz() {
							var nothingHere = foo();
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					var nothingHere = foo();
					    ^^^^^^^^^^^
				Variable initializer is 'void' -- cannot infer variable type
				----------
				""");
}
public void test0009_var_as_type_name() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public enum var { V, A, R };
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public enum var { V, A, R };
					            ^^^
				'var' is not a valid type name
				----------
				""");
}
public void test0010_array_initializer() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void main(String [] args) {
							var myArray = { 1, 2, 3 };
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var myArray = { 1, 2, 3 };
					    ^^^^^^^
				Array initializer needs an explicit target-type
				----------
				""");
}
public void test0011_array_type() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void main(String [] args) {
							var myArray[] = new int[42];
							var[] moreArray = new int[1337];
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var myArray[] = new int[42];
					    ^^^^^^^
				'var' is not allowed as an element type of an array
				----------
				2. ERROR in X.java (at line 4)
					var[] moreArray = new int[1337];
					      ^^^^^^^^^
				'var' is not allowed as an element type of an array
				----------
				""");
}
public void test0012_self_reference() throws IOException {

	// BTW: This will give a VerifyError: int a = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : a)).call();
	// The cases are tested. a is a simple case, with plain usage in the same scope. b is used in a nested scope.
	// c and d are shadowed by the nested definitions.
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void main(String [] args) {
							var a = 42 + a;
							var b = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : b)).call();
					       var c = new java.util.concurrent.Callable<Integer>() {
					           public Integer call() {
					               int c = 42; return c;
					           }
					       }.call();\
					       var d = new java.util.concurrent.Callable<Integer>() {
					           int d = 42;
					           public Integer call() {
					               return d;
					           }
					       }.call();\
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var a = 42 + a;
					    ^
				Declaration using 'var' may not contain references to itself
				----------
				2. ERROR in X.java (at line 4)
					var b = ((java.util.concurrent.Callable<Integer>)(() -> true ? 1 : b)).call();
					    ^
				Declaration using 'var' may not contain references to itself
				----------
				3. WARNING in X.java (at line 7)
					int c = 42; return c;
					    ^
				The local variable c is hiding another local variable defined in an enclosing scope
				----------
				3. WARNING in X.java (at line 10)
					int d = 42;
					    ^
				The field new Callable<Integer>(){}.d is hiding another local variable defined in an enclosing scope
				----------
				""");
}
public void test0013_lambda() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void main(String [] args) {
							var a = (int i) -> 42;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var a = (int i) -> 42;
					    ^
				Lambda expression needs an explicit target-type
				----------
				""");
}
public void test0014_method_reference() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void main(String [] args) {
							var a = X::main;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var a = X::main;
					    ^
				Method reference needs an explicit target-type
				----------
				""");
}
public void test0015_complain_over_first_poly_encountered() throws Exception {

	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void main(String [] args) {
							var a = args.length > 1 ? X::main : (int i) -> 42;
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					var a = args.length > 1 ? X::main : (int i) -> 42;
					    ^
				Method reference needs an explicit target-type
				----------
				""");
}
public void test0016_dont_capture_deep_poly_expressions() throws IOException {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String [] args) throws Exception {
							var z = ((java.util.concurrent.Callable<String>)(() -> "SUCCESS"));
							var x = args.length > 1 ? "FAILURE" : z.call();
							System.out.println(x);
						}
					}
					"""
			},
			"SUCCESS");
}
//static <T extends List<? super E>, E extends List<? super Integer>> void doSomething(T[] e) {
//	e[0] = null;
//}

public void test0017_simple_variable_types() throws Exception {
	InferredTypeVerifier typeVerifier = new InferredTypeVerifier();
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					
					public class X {
					    void test() {
					        var i_String = "";
					        for (var i2_String = "" ; ; ) { break; }
					        for (var i2_String : iterableOfString()) { break; }
					        for (var i2_String : arrayOfString()) { break; }
					        try (var i2_Bar = new Bar()) { } finally { }
					        try (var i2_Bar = new Bar(); var i3_Bar = new Bar()) { } finally { }
					    }
					
					    Iterable<String> iterableOfString() { return null; }
					    String[] arrayOfString() { return null; }
					
					    static class Bar implements AutoCloseable {
					        @Override
					        public void close() { }
					    }
					}
					
					"""
			},
			typeVerifier);
	Assert.assertEquals(7, typeVerifier.localsChecked);
}
public void test0018_primitive_variable_types() throws Exception {
	InferredTypeVerifier typeVerifier = new InferredTypeVerifier();
	this.runConformTest(
			new String[] {
				"Y.java",
				"""
					class Y {
					    boolean[] booleanArray = { true };
					    byte[] byteArray = { 1 };
					    char[] charArray = { 'c' };
					    short[] shortArray = { 42 };
					    int[] intArray = { 42 };
					    long[] longArray = { 42L };
					    float[] floatArray = { 0.1f };
					    double[] doubleArray = { 0.2d };
					
					    void testBuiltins() {
					        var z_boolean = false;
					        var b_byte = (byte)0xff;
					        var c_char = 'c';
					        var s_short = (short)42;
					        var i_int = 42;
					        var l_long = 42L;
					        var f_float = 0.25f;
					        var d_double = 0.35d;
					    }
					
					    void testBuiltinsForEach() {
					        for (var z_boolean : booleanArray) { System.out.print("."); }
					        for (var b_byte : byteArray) { System.out.print("."); }
					        for (var c_char : charArray) { System.out.print("."); }
					        for (var s_short : shortArray) { System.out.print("."); }
					        for (var i_int : intArray) { System.out.print("."); }
					        for (var l_long : longArray) { System.out.print("."); }
					        for (var f_float : floatArray) { System.out.print("."); }
					        for (var d_double : doubleArray) { System.out.print("."); }
					    }
					    void testBuiltinsArray() {
					        var z_booleanARRAY = booleanArray;
					        var b_byteARRAY = byteArray;
					        var c_charARRAY = charArray;
					        var s_shortARRAY = shortArray;
					        var i_intARRAY = intArray;
					        var l_longARRAY = longArray;
					        var f_floatARRAY = floatArray;
					        var d_doubleARRAY = doubleArray;
					    }
					
					}
					"""
			},
			typeVerifier);
	Assert.assertEquals(24, typeVerifier.localsChecked);
}
public void test0018_project_variable_types() throws Exception {
	InferredTypeVerifier typeVerifier = new InferredTypeVerifier();
	this.runConformTest(
			new String[] {
				"Z.java",
				"""
					import java.util.Collection;
					import java.util.List;
					import java.io.Serializable;
					
					class Z {
					
					    void testExtends() {
					        var l1_CollectionOfExtString = extendsString();
					        for (var l2_CollectionOfExtString = extendsString() ; ; ) { break; }
					        for (var l3_CollectionOfExtString : extendsStringArr()) { break; }
					        for (var l4_CollectionOfExtString : extendsCollectionIterable()) { break; }
					        for (var l5_String : extendsString()) { break; }
					    }
					
					    void testExtendsFbound() {\s
					        var l1_CollectionExt_ComparableAny = extendsTBound();
					        for (var l2_CollectionExt_ComparableAny = extendsTBound() ; ; ) { break; }
					        for (var l3_CollectionExt_ComparableAny : extendsTBoundArray()) { break; }
					        for (var l3_CollectionExt_ComparableAny : extendsTBoundIter()) { break; }
					        for (var l4_ComparableAny : extendsTBound()) { break; }
					    }
					
					    void testSuperTBound() {
					        var s_CollectionAny = superTBound();
					        for (var s2_CollectionAny = superTBound() ; ; ) { break; }
					        for (var s2_CollectionAny : superTBoundArray()) { break; }
					        for (var s2_CollectionAny : superTBoundIter()) { break; }
					        for (var s2_Object : superTBound()) { break; }
					    }
					
					    void testCollectSuper() {
					        var s_CollectionOfSuperString = superString();
					        for (var s2_CollectionOfSuperString = superString() ; ; ) { break; }
					        for (var s2_CollectionOfSuperString : superStringArray()) { break; }
					        for (var s2_CollectionOfSuperString : superCollectionIterable()) { break; }
					        for (var s2_Object : superString()) { break; }
					    }
					
					    void testUnbound() {
					        var s_CollectionAny = unboundedString();
					        for (var s2_CollectionAny = unboundedString() ; ; ) { break; }
					        for (var s2_CollectionAny : unboundedStringArray()) { break; }
					        for (var s2_CollectionAny : unboundedCollectionIterable()) { break; }
					        for (var s2_Object : unboundedString()) { break; }
					    }
					
					    void testTypeOfAnAnonymousClass() {
					        var o_AnonymousObjectSubclass = new Object() { };
					        for (var s2_AnonymousObjectSubclass = new Object() { } ; ; ) { break; }
					        for (var s2_AnonymousObjectSubclass : arrayOf(new Object() { })) { break; }
					        for (var s2_AnonymousObjectSubclass : listOf(new Object() { })) { break; }
					    }
					
					    void testTypeOfAnAnonymousInterface() {
					        var r_AnonymousRunnableSubclass = new Runnable() { public void run() { } };
					        for (var s2_AnonymousRunnableSubclass = new Runnable() { public void run() { } } ; ; ) { break; }
					        for (var s2_AnonymousRunnableSubclass : arrayOf(new Runnable() { public void run() { } })) { break; }
					        for (var s2_AnonymousRunnableSubclass : listOf(new Runnable() { public void run() { } })) { break; }
					    }
					
					    void testTypeOfIntersectionType() {
					        var c_IntLongFloat = choose(1, 1L);
					        for (var s2_IntLongFloat = choose(1, 1L) ; ;) { break; }
					        for (var s2_IntLongFloat : arrayOf(choose(1, 1L))) { break; }
					        for (var s2_IntLongFloat : listOf(choose(1, 1L))) { break; }
					    }
					
					    public void testProjections() {
					        var inter_ListTestAndSerializable = getIntersections();
					        var r_TestAndSerializable = inter_ListTestAndSerializable.get(0);
					    }
					
					    Collection<? extends String> extendsString() { return null; }
					    Collection<? super String> superString() { return null; }
					    Collection<?> unboundedString() { return null; }
					
					    Collection<? extends String>[] extendsStringArr() { return null; }
					    Collection<? super String>[] superStringArray() { return null; }
					    Collection<?>[] unboundedStringArray() { return null; }
					
					    Iterable<? extends Collection<? extends String>> extendsCollectionIterable() { return null; }
					    Iterable<? extends Collection<? super String>> superCollectionIterable() { return null; }
					    Iterable<? extends Collection<?>> unboundedCollectionIterable() { return null; }
					
					    <TBound extends Comparable<TBound>> Collection<? extends TBound> extendsTBound() { return null; }
					    <TBound extends Comparable<TBound>> Collection<? super TBound> superTBound() { return null; }
					
					    <TBound extends Comparable<TBound>> Collection<? extends TBound>[] extendsTBoundArray() { return null; }
					    <TBound extends Comparable<TBound>> Collection<? super TBound>[] superTBoundArray() { return null; }
					
					    <TBound extends Comparable<TBound>> Iterable<? extends Collection<? extends TBound>> extendsTBoundIter() { return null; }
					    <TBound extends Comparable<TBound>> Iterable<? extends Collection<? super TBound>> superTBoundIter() { return null; }
					
					    <TBound> Collection<TBound> listOf(TBound b) { return null; }
					    <TBound> TBound[] arrayOf(TBound b) { return null; }
					
					    <TBound> TBound choose(TBound b1, TBound b2) { return b1; }
					    <T extends Z & Serializable> List<? extends T> getIntersections() {
					        return null;
					    }
					}"""
			},
			typeVerifier);
	Assert.assertEquals(39, typeVerifier.localsChecked);
}
public void testBug531832() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					        for (var[] v : args) { }
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					for (var[] v : args) { }
					           ^
				'var' is not allowed as an element type of an array
				----------
				""");
}
public void testBug530879() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void foo() { }
					    public static void main(String [] args) {
					        for (var v : foo()) { }
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					for (var v : foo()) { }
					         ^
				Variable initializer is 'void' -- cannot infer variable type
				----------
				2. ERROR in X.java (at line 4)
					for (var v : foo()) { }
					             ^^^^^
				Can only iterate over an array or an instance of java.lang.Iterable
				----------
				""");
}
public void testBug530879a() throws IOException {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					    public static void main(String [] args) {
					        for (var v : null) { }
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					for (var v : null) { }
					         ^
				Cannot infer type for local variable initialized to 'null'
				----------
				2. ERROR in X.java (at line 3)
					for (var v : null) { }
					             ^^^^
				Can only iterate over an array or an instance of java.lang.Iterable
				----------
				""");
}
public void testBug532349() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo(Boolean p) {
						Y<? super Boolean> y = new Y<>();
						var v = y;
						Y<? super Boolean> tmp = v;
					}
				}
				class Y<T extends Boolean> {
				}"""
		});
}
public void testBug532349a() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				public class X {
					public static void foo(Boolean p) {
						List<Y<? super Boolean>> l = new ArrayList<>();
						var dlv = l;
						for (var iv : dlv) {
							Y<? super Boolean> id = iv;
						}\
					}
				}
				class Y<T extends Boolean> {}"""
		});
}
public void testBug532349b() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo(Boolean p) {
						Y<? super Boolean> y = new Y<>();
						try (var v = y) {
							Y<? super Boolean> tmp = v;
						} catch (Exception e) { }
					}
				}
				class Y<T extends Boolean> implements AutoCloseable {
					@Override
					public void close() throws Exception {}
				}"""
		});
}
public void testBug532351() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void foo(Boolean p) {
				    Y<? super Number> y = new Y<Number>(); // Javac reports, ECJ accepts
				    var v = y;
				    Y<? super Number> tmp = v;
				  }
				  class Y<T extends Number> {
				  }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				Y<? super Number> y = new Y<Number>(); // Javac reports, ECJ accepts
				                      ^^^^^^^^^^^^^^^
			No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).
			----------
			""");
}
public void testBug531025() {
	runNegativeTest(
		new String[] {
			"a/Ann.java",
			"package a;\n" +
			"public @interface Ann {}\n",
			"a/AnnM.java",
			"""
				package a;
				import java.lang.annotation.*;
				@Target(ElementType.METHOD)
				public @interface AnnM {}
				""",
			"a/AnnD.java",
			"""
				package a;
				import java.lang.annotation.*;
				@Target(ElementType.LOCAL_VARIABLE)
				public @interface AnnD {}
				""",
			"a/AnnT.java",
			"""
				package a;
				import java.lang.annotation.*;
				@Target(ElementType.TYPE_USE)
				public @interface AnnT {}
				""",
			"a/AnnDT.java",
			"""
				package a;
				import java.lang.annotation.*;
				@Target({ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
				public @interface AnnDT {}
				""",
			"X.java",
			"""
				import a.*;
				import java.util.*;
				public class X {
					void test(List<String> strings) {
						@Ann   var v  = strings;
						@AnnM  var vm = strings;
						@AnnD  var vd = strings;
						@AnnT  var vt = "";
						@AnnDT var vdt = this;
						for (@AnnD var fvd : strings) {}
						for (@AnnT var fvt : strings) {}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				@AnnM  var vm = strings;
				^^^^^
			The annotation @AnnM is disallowed for this location
			----------
			2. ERROR in X.java (at line 8)
				@AnnT  var vt = "";
				^^^^^
			The annotation @AnnT is disallowed for this location
			----------
			3. ERROR in X.java (at line 11)
				for (@AnnT var fvt : strings) {}
				     ^^^^^
			The annotation @AnnT is disallowed for this location
			----------
			""");
}
public void testBug532349_001() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo() {
						Y<? extends Number> y = new Y<>();
						var v = y.t;
						Integer dsbType0 = v;
					}
				}
				class Y<T extends Integer> {
					public T t;
				}"""
		});
}
public void testBug532349_002() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo() {
						Y<? extends I> y = new Y<>();
						var v = y.t;
						Integer dsbType0 = v;
					}
				}
				interface I { }
				class Y<T extends Integer> {
					public T t;
				}"""
		});
}
public void testBug532349_003() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo(Y<? extends I> y) {
						var v = y.t;
						Integer dsbType0 = v;
						I i = v;
					}
				}
				interface I { }
				class Y<T extends Integer> {
					public T t;
				}"""
		});
}
public void testBug532349_004() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				class X {
					public static void foo() {
						Y<? extends Integer> y = new Y<>();
						var v = y.t;
						Integer dsbType0 = v;
						Serializable s = v;
					}
				}
				class Y<T extends Number&Serializable> {
					public T t;
				}"""
		});
}
public void testBug532349_005() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				class X {
					public static void foo() {
						Y<?> y = new Y<>();
						var v = y.t;
						I i = v;
						Serializable s = v;
					}
				}
				interface I { }
				class Y<T extends I&Serializable> {
					public T t;
				}"""
		});
}
public void testBug532349_006() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				class X {
					public static void foo() {
						Y<? extends I> y = new Y<>();
						var v = y.t;
						I i = v;
						Serializable s = v;
					}
				}
				interface I { }
				class Y<T extends Serializable> {
					public T t;
				}""",
		});
}
public void testBug532349_007() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo() {
						Z<? extends I> z = new Z<>();
						var v = z.t;
						X x = v.t;
						v.doSomething();
					}
				}
				interface I { void doSomething();}
				class Z<T extends Y<?>> {
					public T t;
				}
				class Y<T extends X> {
					public T t;
				}""",
		});
}
public void testBug532349_008() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo() {
						Z<? extends Y<? extends C>> z = new Z<>();
						var v = z.t;
						C c = v.t;
						v.doSomething();
					}
				}
				interface I { void doSomething();}
				class C extends X{ }
				class Z<T extends I> {
					public T t;
				}
				class Y<T extends X> {
					public T t;
				}""",
		});
}
public void testBug532349_009() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				class X {
					public static void foo() {
						Y<? super J> y = new Y<>();
						var v = y.t;
						I i = v;
						Serializable s = v;
					}
				}
				interface I { }
				interface J extends I{}\
				class Y<T extends I> {
					public T t;
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				Serializable s = v;
				                 ^
			Type mismatch: cannot convert from I to Serializable
			----------
			""");
}
public void testBug532349_010() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				class X {
					public static void foo(C<?> c) {
						var v = c.t;
						v = (I&Serializable) new D();
						v.doSomething();
					}
				}
				interface I { void doSomething();}
				class C<T extends I&Serializable>{ T t;}
				class D implements I, Serializable { public void doSomething() {} }
				"""
		});
}
public void testBug532349_11() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					static <R extends D<? extends Y>> W<? extends R> boo() {
						return null;
					}
					public static void foo() {
						var v = boo();
						var var = v.t;
						Y y = var.r;
					}
				}
				class Y extends X { }
				class D<R extends X>{ R r;}
				class W<T extends D<?>> { T t; }
				"""
		});
}
public void testBug532349_12() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo(D<?> d) {
						var v = d;
						D<? extends Y> dy = v;
						D<? extends X> dx = v;
					}
				}
				class Y extends X{ }
				class D<R extends Y>{ R r;}
				"""
		});
}
public void testBug532349_13() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo(D<Y<? extends Integer>> d) {
						var v = d.r;
						Y<? extends Number> yn = v;
						Y<? extends Integer> yi = v;
					}
				}
				class Y<T extends Integer>{ }
				class D<R extends Y<? extends Number>>{ R r;}
				"""
		});
}
public void testBug532349_14() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo(A<? super C> ac) {
						C c = new C(100);
						var c1 = ac;
						A<? super C> a1 = c1;
						A<? super C> a2 = new A<B>(new B());
						a2 = c1;
					}
				}
				class C<T> extends B{
					T t;
					C(T t) {
						this.t = t;
					}
				}
				class B { }
				class A<Q> {
					A(Q e) {}
				}"""
		});
}
public void testBug532349_15() throws IOException {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					    public static <T> A<T> m(T t) {
				        return new A(t);
				    }
				    public static <U extends I1<?>> A<? extends U> m2(A<? super U> u) {
				        return new A(u);
				    }
				    public static void main(String argv[]) {
				        A<?> checkValue1 = new C(10);
				        var varValue = m2(m(checkValue1));
				        if(!varValue.t.t.equals(10)) {
				            System.out.println("Error:");
				        }
				        if(varValue.t.methodOnI1() != true) {
				            System.out.println("Error:");
				        }
				    }\
				}
				class A<E> {
				    E t;
				    A(E t) {
				        this.t = t;
				    }
				    A<E> u;
				    A (A<E> u) {
				        this(u.t);
				        this.u = u;
				    }
				}
				interface I1<E> {
				    default boolean methodOnI1() {
				        return true;
				    }
				}
				class C<T> extends A implements I1 {
				    C(T t) {
				        super(t);
				    }
				}"""
		}, "");
}
public void testBug532349_0016() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static void foo() {
						Y<? extends I> yi = new Y<>();
						var vi = yi.t;
						Y<Integer> yj = new Y<>();
						vi = yj.t;
					}
				}
				interface I { }
				class Y<T extends Number> {
					public T t;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				vi = yj.t;
				     ^^^^
			Type mismatch: cannot convert from Integer to Number & I
			----------
			""");
}
public void testBug532349_0017() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
					public static <Q extends Number & I> void foo(Y<? super Q> y) {
						var vy = y;
						Y<Integer> yi = new Y<>();
						vy = yi;
					}
				}
				interface I { }
				class Y<T extends Number> {
					public T t;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				vy = yi;
				     ^^
			Type mismatch: cannot convert from Y<Integer> to Y<? super Q>
			----------
			""");
}
public void testBug532920() throws IOException {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Iterator;
				public class X {
				  static void foo(Z<?> ef) { \s
				    for (var l : ef.t) {
				      l = new Object();
				    }
				  }
				}
				class I<T> {// implements Iterable<T> {
				 T t;
				}
				class Q {}
				class Y extends Q{ }
				class Z<T extends Iterable<? super Y>> {
				  I<T> t;
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				for (var l : ef.t) {
				             ^^^^
			Can only iterate over an array or an instance of java.lang.Iterable
			----------
			""");
}
public void testBug567183_1() {
	this.runNegativeTest(
			new String[] {
				"p/Item.java",
				"""
					package p;
					class Item {
					}""",
				"p/Container.java",
				"""
					package p;
					import java.util.List;
					
					public class Container {
					  public final List<Item> items = null;
					}""",
				"p/PublicItem.java",
				"""
					package p;
					public class PublicItem extends Item {
					}""",
				"p1/X.java",
				"package p1;\n"
				+ "import p.Container;\n"
				+ "import p.PublicItem;\n"
				+ "public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		var container = new Container();\n"
				+ "		for (var item : container.items) {\n"
				+ "			if (item instanceof PublicItem) {\n"
				+ "				var publicItem = (PublicItem) item;\n"
				+ "				System.out.println(publicItem);\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "}\n"
				+ ""
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 7)
					for (var item : container.items) {
					         ^^^^
				The type Item is not visible
				----------
				""");
}
public void testBug567183_2() {
	this.runNegativeTest(
			new String[] {
				"p/Item.java",
				"""
					package p;
					class Item {
					}""",
				"p/Container.java",
				"""
					package p;
					import java.util.List;
					
					public class Container {
					  public final List<List<Item>> items = null;
					}""",
				"p/PublicItem.java",
				"""
					package p;
					public class PublicItem extends Item {
					}""",
				"p1/X.java",
				"package p1;\n"
				+ "import java.util.List;\n"
				+ "import p.Container;\n"
				+ "import p.PublicItem;\n"
				+ "public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		var container = new Container();\n"
				+ "		for (var item : container.items) {\n"
				+ "			if (item instanceof PublicItem) {\n"
				+ "				var publicItem = (PublicItem) item;\n"
				+ "				System.out.println(publicItem);\n"
				+ "			} else if (item instanceof List) {\n"
				+ "				  for (var item2 : item) {}\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}\n"
				+ "}\n"
				+ ""
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 13)
					for (var item2 : item) {}
					         ^^^^^
				The type Item is not visible
				----------
				""");
}
public void testBug567183_3() {
	this.runNegativeTest(
			new String[] {
				"p/Item.java",
				"""
					package p;
					class Item {
					}""",
				"p/Container.java",
				"""
					package p;
					import java.util.List;
					
					public class Container {
					  public final List<List<Item>> items = null;
					}""",
				"p/PublicItem.java",
				"""
					package p;
					public class PublicItem extends Item {
					}""",
				"p1/X.java",
				"package p1;\n"
				+ "import p.Container;\n"
				+ "public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		var container = new Container();\n"
				+ "		for (var item : container.items) { // Javac over-eagerly reports this\n"
				+ "		}\n"
				+ "	}\n"
				+ "}\n"
				+ ""
			},
			"");
}
public void testBug567183_4() {
	this.runNegativeTest(
			new String[] {
				"p/Item.java",
				"""
					package p;
					class Item {
					}""",
				"p/Container.java",
				"""
					package p;
					import java.util.List;
					
					public class Container {
					  public final List<List<Item>> items = null;
					}""",
				"p/PublicItem.java",
				"""
					package p;
					public class PublicItem extends Item {
					}""",
				"p1/X.java",
				"package p1;\n"
				+ "import p.Container;\n"
				+ "public class X {\n"
				+ "	public static void main(String[] args) {\n"
				+ "		var container = new Container();\n"
				+ "		var item1 = container.items.get(0).get(0);\n"
				+ "	}\n"
				+ "}\n"
				+ ""
			},
			"""
				----------
				1. ERROR in p1\\X.java (at line 6)
					var item1 = container.items.get(0).get(0);
					            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The type Item is not visible
				----------
				""");
}
public void testIssue600_1() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.List;
					public class X {
						public static void main(String [] args) {
							var<Integer> x = List.of(42);
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					var<Integer> x = List.of(42);
					^^^
				'var' cannot be used with type arguments
				----------
				""");
}
public void testIssue600_2() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String [] args) {
							for (var<Integer> i = 0; i < 5; i++) {
								System.out.println(i);
							}\
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					for (var<Integer> i = 0; i < 5; i++) {
					     ^^^
				'var' cannot be used with type arguments
				----------
				""");
}
public void testIssue600_3() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						import java.util.List;
						public class X {
							public static void main(String [] args) {
								for (var<Integer> i : List.of(2, 3, 5)) {
									System.out.println(i);
								}\
							}
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					for (var<Integer> i : List.of(2, 3, 5)) {
					     ^^^
				'var' cannot be used with type arguments
				----------
				""");
}
public void testIssue600_4() {
	this.runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						    public static void main(String [] args) throws Exception {
								try(var<String> w = new java.io.StringWriter()) {
									w.write("SUCCESS\\n");\
									System.out.println(w.toString());
						       }
						    }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					try(var<String> w = new java.io.StringWriter()) {
					    ^^^
				'var' cannot be used with type arguments
				----------
				""");
}
}
