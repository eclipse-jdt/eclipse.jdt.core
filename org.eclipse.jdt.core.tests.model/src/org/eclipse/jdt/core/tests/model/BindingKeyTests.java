/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.Signature;

import junit.framework.Test;

public class BindingKeyTests extends AbstractJavaModelTests {

	static {
//		TESTS_PREFIX = "testInvalidCompilerOptions";
//		TESTS_NAMES = new String[] { "test028"};
//		TESTS_NUMBERS = new int[] { 56 };
	}

	public BindingKeyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(BindingKeyTests.class);
	}

	protected void assertBindingKeyEquals(String expected, String key) {
		if (!(expected.equals(key)))
			System.out.println(displayString(key, 3) + ",");
		assertEquals(expected, key);
	}

	protected void assertBindingKeySignatureEquals(String expected, String key) {
		BindingKey bindingKey = new BindingKey(key);
		String signature = bindingKey.toSignature();
		if (!(expected.equals(signature)))
			System.out.println(displayString(signature, 3) + ",");
		assertEquals(expected, signature);
	}

	protected void assertBindingKeyTypeArgumentsEqual(String expected, String key) {
		BindingKey bindingKey = new BindingKey(key);
		String[] typeArguments = bindingKey.getTypeArguments();
		assertStringsEqual("Unexpected type arguments", expected, typeArguments);
	}

	protected void assertBindingKeyDeclaringTypesEqual(String expected, String key) {
		BindingKey bindingKey = new BindingKey(key);
		BindingKey declaringTypeBindingKey = bindingKey.getDeclaringType();
		if (expected == null) {
			assertNull("Unexpected declaring type",declaringTypeBindingKey);
		} else {
			String signature = declaringTypeBindingKey.toSignature();
			assertEquals("Unexpected declaring type", expected, signature);
		}
	}
	/*
	 * Package.
	 */
	public void test001() {
		assertBindingKeySignatureEquals(
			"p",
			"p"
		);
	}

	/*
	 * Top level type in non default package.
	 */
	public void test002() {
		assertBindingKeySignatureEquals(
			"Lp.X;",
			"Lp/X;"
		);
	}

	/*
	 * Top level type in default package.
	 */
	public void test003() {
		assertBindingKeySignatureEquals(
			"LClazz;",
			"LClazz;"
		);
	}

	/*
	 * Member type
	 */
	public void test004() {
		assertBindingKeySignatureEquals(
			"Lp.X$Member;",
			"Lp/X$Member;"
		);
	}

	/*
	 * Member type (2 levels deep)
	 */
	public void test005() {
		assertBindingKeySignatureEquals(
			"Lp1.X$Member1$Member2;",
			"Lp1/X$Member1$Member2;"
		);
	}

	/*
	 * Anonymous type
	 */
	public void test006() {
		assertBindingKeySignatureEquals(
			"Lp1.X$1;",
			"Lp1/X$1;"
		);
	}

	/*
	 * Local type
	 */
	public void test007() {
		assertBindingKeySignatureEquals(
			"Lp1.X$1$Y;",
			"Lp1/X$1$Y;"
		);
	}

	/*
	 * Array type
	 */
	public void test008() {
		assertBindingKeySignatureEquals(
			"[Lp1.X;",
			"[Lp1/X;"
		);
	}

	/*
	 * Generic type
	 */
	public void test009() {
		assertBindingKeySignatureEquals(
			"<T:Ljava/lang/Object;>Lp1.X;",
			"Lp1/X<TT;>;"
		);
	}

	/*
	 * Generic type
	 */
	public void test010() {
		assertBindingKeySignatureEquals(
			"<T:Ljava/lang/Object;U:Ljava/lang/Object;>Lp1.X;",
			"Lp1/X<TT;TU;>;"
		);
	}

	/*
	 * Parameterized type
	 */
	public void test011() {
		assertBindingKeySignatureEquals(
			"Lp1.X<Ljava.lang.String;>;",
			"Lp1/X<Ljava/lang/String;>;"
		);
	}

	/*
	 * Secondary type
	 */
	public void test012() {
		assertBindingKeySignatureEquals(
			"Lp1.Secondary;",
			"Lp1/X~Secondary;"
		);
	}

	/*
	 * Anonymous in a secondary type
	 */
	public void test013() {
		assertBindingKeySignatureEquals(
			"Lp1.Secondary$1;",
			"Lp1/X~Secondary$1;"
		);
	}

	/*
	 * Method
	 * (regression test for bug 85811 BindingKey.toSignature should return method signature for methods)
	 */
	public void test014() {
		assertBindingKeySignatureEquals(
			"(Ljava.lang.String;I)Z",
			"Lp1/X;.foo(Ljava/lang/String;I)Z"
		);
	}

	/*
	 * Create a type binding key from a fully qualified name
	 */
	public void test015() {
		String key = BindingKey.createTypeBindingKey("java.lang.Object");
		assertBindingKeyEquals(
			"Ljava/lang/Object;",
			key);
	}

	/*
	 * Create a type binding key from a primitive type name
	 */
	public void test016() {
		String key = BindingKey.createTypeBindingKey("int");
		assertBindingKeyEquals(
			"I",
			key);
	}

	/*
	 * Create a type binding key from an array type name
	 */
	public void test017() {
		String key = BindingKey.createTypeBindingKey("boolean[]");
		assertBindingKeyEquals(
			"[Z",
			key);
	}

	/*
	 * Create a parameterized type binding key
	 */
	public void test018() {
		String key = BindingKey.createParameterizedTypeBindingKey("Ljava/util/Map<TK;TV;>;", new String[] {"Ljava/lang/String;", "Ljava/lang/Object;"});
		assertBindingKeyEquals(
			"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;",
			key);
	}

	/*
	 * Create a raw type binding key
	 */
	public void test019() {
		String key = BindingKey.createParameterizedTypeBindingKey("Ljava/util/List<TE:>;", new String[] {});
		assertBindingKeyEquals(
			"Ljava/util/List<>;",
			key);
	}

	/*
	 * Create an array type binding key
	 */
	public void test020() {
		String key = BindingKey.createArrayTypeBindingKey("Ljava/lang/Object;", 1);
		assertBindingKeyEquals(
			"[Ljava/lang/Object;",
			key);
	}

	/*
	 * Create an array type binding key
	 */
	public void test021() {
		String key = BindingKey.createArrayTypeBindingKey("I", 2);
		assertBindingKeyEquals(
			"[[I",
			key);
	}

	/**
	 * @deprecated
	 */
	private String getWildcardBindingKey(String typeKey, char kind) {
		return BindingKey.createWilcardTypeBindingKey(typeKey, kind);
	}

	/*
	 * Create a wildcard type binding key
	 */
	public void test022() {
		String key = getWildcardBindingKey(null, Signature.C_STAR);
		assertBindingKeyEquals(
			"*",
			key);
		assertBindingKeyEquals(BindingKey.createWildcardTypeBindingKey(
				BindingKey.createTypeBindingKey("java.util.ArrayList"), Signature.C_STAR, null, 0),
				"Ljava/util/ArrayList;{0}*");
	}

	/*
	 * Create a wildcard type binding key
	 */
	public void test023() {
		String key = getWildcardBindingKey("Ljava/util/List<TE;>;", Signature.C_SUPER);
		assertBindingKeyEquals(
			"-Ljava/util/List<TE;>;",
			key);
		assertBindingKeyEquals(BindingKey.createWildcardTypeBindingKey(
				BindingKey.createTypeBindingKey("java.util.ArrayList"), Signature.C_SUPER,
				BindingKey.createTypeBindingKey("java.lang.String"), 0),
				"Ljava/util/ArrayList;{0}-Ljava/lang/String;"
				);
	}

	/*
	 * Create a wildcard type binding key
	 */
	public void test024() {
		String key = getWildcardBindingKey("Ljava/util/ArrayList;", Signature.C_EXTENDS);
		assertBindingKeyEquals(
			"+Ljava/util/ArrayList;",
			key);
		assertBindingKeyEquals(BindingKey.createWildcardTypeBindingKey(
				BindingKey.createTypeBindingKey("java.util.ArrayList"), Signature.C_EXTENDS,
				BindingKey.createTypeBindingKey("java.lang.String"), 0),
				"Ljava/util/ArrayList;{0}+Ljava/lang/String;"
				);
	}

	/*
	 * Create a type variable binding key
	 */
	public void test025() {
		String key = BindingKey.createTypeVariableBindingKey("T", "Ljava/util/List<TE;>;");
		assertBindingKeyEquals(
			"Ljava/util/List<TE;>;:TT;",
			key);
	}

	/*
	 * Create a type variable binding key
	 */
	public void test026() {
		String key = BindingKey.createTypeVariableBindingKey("SomeTypeVariable", "Lp/X;.foo()V");
		assertBindingKeyEquals(
			"Lp/X;.foo()V:TSomeTypeVariable;",
			key);
	}

	/*
	 * Parameterized member type
	 */
	public void test027() {
		assertBindingKeySignatureEquals(
			"Lp1.X<Ljava.lang.String;>.Member;",
			"Lp1/X<Ljava/lang/String;>.Member;"
		);
	}

	/*
	 * Wildcard binding (no bounds)
	 */
	public void test028() {
		assertBindingKeySignatureEquals(
			"*",
			"Lp1/X;{0}*"
		);
	}

	/*
	 * Wildcard binding (super bounds)
	 */
	public void test029() {
		assertBindingKeySignatureEquals(
			"-Ljava.util.List<TT;>;",
			"Lp1/X;{0}-Ljava/util/List<Lp1/X;:TT;>"
		);
	}

	/*
	 * Wildcard binding (extends bounds)
	 */
	public void test030() {
		assertBindingKeySignatureEquals(
			"+Ljava.util.ArrayList;",
			"Lp1/X;{0}+Ljava/util/ArrayList;"
		);
	}

	/*
	 * Capture binding (no bounds)
	 */
	public void test031() {
		assertBindingKeySignatureEquals(
			"!*",
			"Ljava/util/List;&!Lp1/X;{0}*123;"
		);
	}

	/*
	 * Capture binding (super bounds)
	 */
	public void test032() {
		assertBindingKeySignatureEquals(
			"!-Ljava.util.List<TT;>;",
			"Ljava/util/List;&!Lp1/X;{0}-Ljava/util/List<Lp1/X;:TT;>;123;"
		);
	}

	/*
	 * Capture binding (extends bounds)
	 */
	public void test033() {
		assertBindingKeySignatureEquals(
			"!+Ljava.util.ArrayList;",
			"Ljava/util/List;&!Lp1/X;{0}+Ljava/util/ArrayList<>;123;"
		);
	}

	/*
	 * Method starting with an upper case corresponding to a primitive type
	 * (regression test for bug 94398 Error attempting to find References)
	 */
	public void test034() {
		assertBindingKeySignatureEquals(
			"(Ljava.lang.String;I)Z",
			"Lp1/X;.Set(Ljava/lang/String;I)Z"
		);
	}

	/*
	 * Parameterized method with capture argument
	 * (regression test for bug 96410 Incorrect information in selection resolved key)
	 */
	public void test035() {
		assertBindingKeySignatureEquals(
			"(!*)!*",
			"LX;&LX~Store<!LX~Store;*157;>;.get(!*)!*"
		);
	}

	/*
	 * Parameterized method with argument similar to a type name
	 */
	public void test036() {
		assertBindingKeySignatureEquals(
			"<U:Ljava.lang.Object;>(La.TU;La.TU;)V",
			"La/A<La/A~TU;>;.foo<U:Ljava/lang/Object;>(TU;La/TU;)V%<La/A~TU;>"
		);
	}

	/*
	 * Field
	 * (regression test for bug  87362 BindingKey#internalToSignature() should return the field's type signature)
	 */
	public void test037() {
		assertBindingKeySignatureEquals(
			"Ljava.lang.String;",
			"Lp/X;.foo)Ljava/lang/String;"
		);
	}

	/*
	 * Generic secondary type
	 * (regression test for bug 96858 IllegalArgumentException in Signature)
	 */
	public void test038() {
		assertBindingKeySignatureEquals(
			"<T:Ljava/lang/Object;U:Ljava/lang/Object;>Lp1.Y;",
			"Lp1/X~Y<TT;TU;>;"
		);
	}

	/*
	 * Base type
	 * (regression test for bug 97187 [rendering] Shows Single Char for primitve Types)
	 */
	public void test039() {
		assertBindingKeySignatureEquals(
			"Z",
			"Z"
		);
	}

	/*
	 * Parameterized method with argument nested in another argument
	 * (regression test for bug 97275 method reference should not contain type variable anymore)
	 */
	public void test040() {
		assertBindingKeySignatureEquals(
			"<T:Ljava.lang.Object;>(Ljava.util.List<Ljava.lang.String;>;Ljava.lang.Integer;)V",
			"Lp1/X;.foo<T:Ljava/lang/Object;>(Ljava/util/List<TT;>;Ljava/lang/Integer;)V%<Ljava/lang/String;>)"
		);
	}

	/*
	 * Parameterized method with argument nested in another argument as a wilcard bound
	 * (regression test for bug 97814 Incorrect resolved information on hover)
	 */
	public void test041() {
		assertBindingKeySignatureEquals(
			"<T:Ljava.lang.Object;>(LY<-Ljava.lang.NullPointerException;>;Ljava.lang.NullPointerException;)V",
			"LX~Z;.foo<T:Ljava/lang/Object;>(LY<-TT;>;TT;)V%<Ljava/lang/NullPointerException;>"
		);
	}

	/*
	 * Parameterized method with argument nested in another argument as an array
	 * (regression test for bug 97814 Incorrect resolved information on hover)
	 */
	public void test042() {
		assertBindingKeySignatureEquals(
			"<T:Ljava.lang.Object;>([Ljava.lang.NullPointerException;Ljava.lang.NullPointerException;)V",
			"LX~Z;.foo<T:Ljava/lang/Object;>([TT;TT;)V%<Ljava/lang/NullPointerException;>"
		);
	}

	/*
	 * Parameterized type binding with a type variable of the current's method in its arguments
	 * (regression test for bug 97902 NPE on Open Declaration on reference to generic type)
	 */
	public void test043() {
		assertBindingKeySignatureEquals(
			"Lp1.Y<TT;>;",
			"Lp1/X~Y<Lp1/X;:40TT;>;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=102710
	 */
	public void test044() {
		assertBindingKeySignatureEquals(
			"<SM:Ljava/lang/Object;LM:Ljava/lang/Object;>LX;",
			"LX<TSM;TLM;>;"
		);
	}

	/*
	 * Ensures that the type arguments for a parameterized type binding key are correct
	 */
	public void test045() {
		assertBindingKeyTypeArgumentsEqual(
			"Ljava.lang.String;\n",
			"LX<Ljava/lang/String;>;"
		);
	}

	/*
	 * Ensures that the type arguments for a parameterized type binding key are correct
	 */
	public void test046() {
		assertBindingKeyTypeArgumentsEqual(
			"Ljava.lang.String;\n" +
			"LY;\n",
			"LX<Ljava/lang/String;LY;>;"
		);
	}

	/*
	 * Ensures that the type arguments for a parameterized type binding key are correct
	 */
	public void test047() {
		assertBindingKeyTypeArgumentsEqual(
			"",
			"LX;"
		);
	}

	/*
	 * Ensures that the type arguments for a parameterized type binding key are correct
	 * (regression test for bug 103654 BindingKey.getTypeArguments bug with qualified types)
	 */
	public void test048() {
		assertBindingKeyTypeArgumentsEqual(
			"Ljava.lang.Object;\n",
			"LX<Ljava/lang/String;>.LY<Ljava/lang/Object;>;"
		);
	}

	/*
	 * Ensures that the type arguments for a parameterized method binding key are correct
	 */
	public void test049() {
		assertBindingKeyTypeArgumentsEqual(
			"Ljava.lang.String;\n",
			"LX;.foo<T:Ljava/lang/Object;>(TT;)V%<Ljava/lang/String;>"
		);
	}

	/*
	 * Parameterized method
	 */
	public void test050() {
		assertBindingKeySignatureEquals(
			"<T:Ljava.lang.Object;>(Ljava.lang.String;)V",
			"LX;.foo<T:Ljava/lang/Object;>(TT;)V%<Ljava/lang/String;>"
		);
	}

	/*
	 * Ensures that the binding key of a parameterized type is not a raw type
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=209475)
	 */
	public void test051() {
		assertFalse("Should not be a raw type", new BindingKey("Ltest/ZZ<Ljava/lang/Object;>;").isRawType());
	}

	/*
	 * Ensures that the signature of a method defined in a cu with a $ name is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=127739 )
	 */
	public void test052() {
		assertBindingKeySignatureEquals(
			"()V",
			"LA$B~A$B;.m()V"
		);
	}

	/*
	 * Ensures that the signature of a method with a $ name is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=127739 )
	 */
	public void test053() {
		assertBindingKeySignatureEquals(
			"()V",
			"LA;.m$1()V"
		);
	}

	/*
	 * Ensures that the signature of a field with a capture return type is correct
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=234172 )
	 */
	public void test054() {
		assertBindingKeySignatureEquals(
			"!*",
			"LX;&LX~Box<!LX~Box;{0}*232;!LX~Box;{1}*232;>;.value)!LX~Box;{0}*232;"
		);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=209479
	 */
	public void test055() {
		assertFalse("Should not be a raw type", new BindingKey("Ltest/ZZ<Ljava/lang/Object>;").isRawType());
	}

	/*
	 * Ensures that the type arguments for a parameterized type binding key are correct for secondary type
	 */
	public void test056() {
		assertBindingKeyTypeArgumentsEqual(
			"[LOuter<Ljava.lang.Integer;>.Inner<Ljava.lang.Double;>;\n",
			"LNullBinding~One<[LNullBinding~Outer<Ljava/lang/Integer;>.Inner<Ljava/lang/Double;>;>;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=336451
	 * Make sure that the binding obtained is a type binding corresponding to the
	 * method type variables
	 */
	public void test057() {
		assertBindingKeySignatureEquals(
			"TT;",
			"LEclipseTest$InvokerIF;.invoke<T::LEclipseTest$ArgIF;>(TT;)TT;|Ljava/lang/RuntimeException;:TT;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=336451
	 * Make sure that the binding obtained is a type binding corresponding to the
	 * method type variables. In this case method has 2 exceptions and 2 type variables.
	 */
	public void test058() {
		assertBindingKeySignatureEquals(
			"TT;",
			"LEclipseTest$InvokerIF;.invoke<T::LEclipseTest$ArgIF;Y:Ljava/lang/Object;>(TT;)TT;|Ljava/lang/RuntimeException;|Ljava/lang/IndexOutOfBoundsException;:TT;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return the correct type for a method
	 */
	public void test059() {
		assertBindingKeyDeclaringTypesEqual(
			"Ljava.util.ArrayList<Ljava.lang.String;>;",
			"Ljava/util/ArrayList<Ljava/lang/String;>;.()V"
		);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return null for a type
	 */
	public void test060() {
		assertBindingKeyDeclaringTypesEqual(
			null,
			"Ljava/util/ArrayList<Ljava/lang/String;>;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return the correct type for
	 * methods of a secondary type
	 */
	public void test061() {
		assertBindingKeyDeclaringTypesEqual(
			"Lpkg.Secondary<Ljava.lang.String;>;",
			"Lpkg/A~Secondary<Ljava/lang/String;>;.()V"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return null for a secondary type
	 */
	public void test062() {
		assertBindingKeyDeclaringTypesEqual(
			null,
			"Lpkg/A~Secondary<Ljava/lang/String;>;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return the correct type for
	 * methods of a secondary's inner type
	 */
	public void test063() {
		assertBindingKeyDeclaringTypesEqual(
			"Lpkg.Secondary<Ljava.lang.String;>.Inner<Ljava.lang.String;>;",
			"Lpkg/A~Secondary<Ljava/lang/String;>.Inner<Ljava/lang/String;>;.(Lpkg/Secondary;)V"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return the correct type for
	 * for a secondary's inner type
	 */
	public void test064() {
		assertBindingKeyDeclaringTypesEqual(
			"Lpkg.Secondary<Ljava.lang.String;>;",
			"Lpkg/A~Secondary<Ljava/lang/String;>.Inner<Ljava/lang/String;>;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return the correct type for
	 * methods of an inner type
	 */
	public void test065() {
		assertBindingKeyDeclaringTypesEqual(
			"Lpkg.A$Inner<Ljava.lang.String;>;",
			"Lpkg/A$Inner<Ljava/lang/String;>;.(Lpkg/A;)V"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return the correct type for an inner type
	 */
	public void test066() {
		assertBindingKeyDeclaringTypesEqual(
			"Lpkg.A;",
			"Lpkg/A$Inner<Ljava/lang/String;>;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return the correct type for
	 * methods of an inner type's inner type
	 */
	public void test067() {
		assertBindingKeyDeclaringTypesEqual(
			"Lpkg.A$Inner1<Ljava.lang.String;>.Inner2<Ljava.lang.String;>;",
			"Lpkg/A$Inner1<Ljava/lang/String;>.Inner2<Ljava/lang/String;>;.(Lpkg/A$Inner1;)V"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return the correct type for
	 * an inner type's inner type
	 */
	public void test068() {
		assertBindingKeyDeclaringTypesEqual(
			"Lpkg.A$Inner1<Ljava.lang.String;>;",
			"Lpkg/A$Inner1<Ljava/lang/String;>.Inner2<Ljava/lang/String;>;"
		);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=351165
	 * getDeclaringType should return null for a local variable
	 */
	public void test069() {
		assertBindingKeyDeclaringTypesEqual(
			null,
			"Lpkg/A;.foo()V#c"
		);
	}
}
