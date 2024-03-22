/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
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

import java.io.File;
import java.io.IOException;
import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;

@SuppressWarnings({ "rawtypes" })
public class ReferenceExpressionSyntaxTest extends AbstractSyntaxTreeTest {

	private static String  jsr335TestScratchArea = "c:\\Jsr335TestScratchArea";
	private static String referenceCompiler = "C:\\jdk-7-ea-bin-b75-windows-i586-30_oct_2009\\jdk7\\bin\\javac.exe"; // TODO: Patch when RI becomes available.

	public static Class testClass() {
		return ReferenceExpressionSyntaxTest.class;
	}
	@Override
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public ReferenceExpressionSyntaxTest(String testName){
		super(testName, referenceCompiler, jsr335TestScratchArea);
		if (referenceCompiler != null) {
			File f = new File(jsr335TestScratchArea);
			if (!f.exists()) {
				f.mkdir();
			}
			CHECK_ALL |= CHECK_JAVAC_PARSER;
		}
	}

	static {
		//		TESTS_NAMES = new String[] { "test0012" };
		//		TESTS_NUMBERS = new int[] { 133, 134, 135 };
		if (!(new File(referenceCompiler).exists())) {
			referenceCompiler = null;
			jsr335TestScratchArea = null;
		}
	}
	// Reference expression - super:: form, without type arguments.
	public void test0001() throws IOException {
		String source =
				"""
			interface I {
			    void foo(int x);
			}
			public class X extends Y {
			    public static void main(String [] args) {
				new X().doit();
			    }
			    void doit() {
			        I i = super::foo;
			        i.foo(10);\s
			    }
			}
			class Y {
			    public void foo(int x) {
				System.out.println(x);
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(int x);
			}
			public class X extends Y {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    new X().doit();
			  }
			  void doit() {
			    I i = super::foo;
			    i.foo(10);
			  }
			}
			class Y {
			  Y() {
			    super();
			  }
			  public void foo(int x) {
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0001", expectedUnitToString);
	}
	// Reference expression - super:: form, with type arguments.
	public void test0002() throws IOException {
		String source =
				"""
			interface I {
			    void foo(int x);
			}
			public class X extends Y {
			    public static void main(String [] args) {
				new X().doit();
			    }
			    void doit() {
			        I i = super::<String>foo;
			        i.foo(10);\s
			    }
			}
			class Y {
			    public void foo(int x) {
				System.out.println(x);
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(int x);
			}
			public class X extends Y {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    new X().doit();
			  }
			  void doit() {
			    I i = super::<String>foo;
			    i.foo(10);
			  }
			}
			class Y {
			  Y() {
			    super();
			  }
			  public void foo(int x) {
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0002", expectedUnitToString);
	}
	// Reference expression - SimpleName:: form, without type arguments.
	public void test0003() throws IOException {
		String source =
				"""
			interface I {
			    void foo(int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y::foo;
			        i.foo(10);\s
			    }
			}
			class Y {
			    public static void foo(int x) {
				System.out.println(x);
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y::foo;
			    i.foo(10);
			  }
			}
			class Y {
			  Y() {
			    super();
			  }
			  public static void foo(int x) {
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0003", expectedUnitToString);
	}
	// Reference expression - SimpleName:: form, with type arguments.
	public void test0004() throws IOException {
		String source =
				"""
			interface I {
			    void foo(int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y::<String>foo;
			        i.foo(10);\s
			    }
			}
			class Y {
			    public static void foo(int x) {
				System.out.println(x);
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y::<String>foo;
			    i.foo(10);
			  }
			}
			class Y {
			  Y() {
			    super();
			  }
			  public static void foo(int x) {
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0004", expectedUnitToString);
	}
	// Reference expression - QualifiedName:: form, without type arguments.
	public void test0005() throws IOException {
		String source =
				"""
			interface I {
			    void foo(int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y.Z::foo;
			        i.foo(10);\s
			    }
			}
			class Y {
			    static class Z {
			        public static void foo(int x) {
				    System.out.println(x);
			        }
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y.Z::foo;
			    i.foo(10);
			  }
			}
			class Y {
			  static class Z {
			    Z() {
			      super();
			    }
			    public static void foo(int x) {
			      System.out.println(x);
			    }
			  }
			  Y() {
			    super();
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0005", expectedUnitToString);
	}
	// Reference expression - QualifiedName:: form, with type arguments.
	public void test0006() throws IOException {
		String source =
				"""
			interface I {
			    void foo(int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y.Z::<String>foo;
			        i.foo(10);\s
			    }
			}
			class Y {
			    static class Z {
			        public static void foo(int x) {
				    System.out.println(x);
			        }
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y.Z::<String>foo;
			    i.foo(10);
			  }
			}
			class Y {
			  static class Z {
			    Z() {
			      super();
			    }
			    public static void foo(int x) {
			      System.out.println(x);
			    }
			  }
			  Y() {
			    super();
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0006", expectedUnitToString);
	}
	// Reference expression - Primary:: form, without type arguments.
	public void test0007() throws IOException {
		String source =
				"""
			interface I {
			    void foo(int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = new Y()::foo;
			        i.foo(10);\s
			    }
			}
			class Y {
			        void foo(int x) {
				    System.out.println(x);
			        }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = new Y()::foo;
			    i.foo(10);
			  }
			}
			class Y {
			  Y() {
			    super();
			  }
			  void foo(int x) {
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0007", expectedUnitToString);
	}
	// Reference expression - primary:: form, with type arguments.
	public void test0008() throws IOException {
		String source =
				"""
			interface I {
			    void foo(int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = new Y()::<String>foo;
			        i.foo(10);\s
			    }
			}
			class Y {
			        void foo(int x) {
				    System.out.println(x);
			        }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = new Y()::<String>foo;
			    i.foo(10);
			  }
			}
			class Y {
			  Y() {
			    super();
			  }
			  void foo(int x) {
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0008", expectedUnitToString);
	}
	// Reference expression - X<T>:: form, without type arguments.
	public void test0009() throws IOException {
		String source =
				"""
			interface I {
			    void foo(Y<String> y, int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y<String>::foo;
			        i.foo(new Y<String>(), 10);\s
			    }
			}
			class Y<T> {
			        void foo(int x) {
				    System.out.println(x);
			        }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(Y<String> y, int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y<String>::foo;
			    i.foo(new Y<String>(), 10);
			  }
			}
			class Y<T> {
			  Y() {
			    super();
			  }
			  void foo(int x) {
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0009", expectedUnitToString);
	}
	// Reference expression - X<T>:: form, with type arguments.
	public void test0010() throws IOException {
		String source =
				"""
			interface I {
			    void foo(Y<String> y, int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y<String>::<String>foo;
			        i.foo(new Y<String>(), 10);\s
			    }
			}
			class Y<T> {
			        void foo(int x) {
				    System.out.println(x);
			        }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(Y<String> y, int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y<String>::<String>foo;
			    i.foo(new Y<String>(), 10);
			  }
			}
			class Y<T> {
			  Y() {
			    super();
			  }
			  void foo(int x) {
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0010", expectedUnitToString);
	}
	// Reference expression - X<T>.Name:: form, without type arguments.
	public void test0011() throws IOException {
		String source =
				"""
			interface I {
			    void foo(Y<String>.Z z, int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y<String>.Z::foo;
			        i.foo(new Y<String>().new Z(), 10);\s
			    }
			}
			class Y<T> {
			    class Z {
			        void foo(int x) {
				    System.out.println(x);
			        }
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(Y<String>.Z z, int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y<String>.Z::foo;
			    i.foo(new Y<String>().new Z(), 10);
			  }
			}
			class Y<T> {
			  class Z {
			    Z() {
			      super();
			    }
			    void foo(int x) {
			      System.out.println(x);
			    }
			  }
			  Y() {
			    super();
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0011", expectedUnitToString);
	}
	// Reference expression - X<T>.Name:: form, with type arguments.
	public void test0012() throws IOException {
		String source =
				"""
			interface I {
			    void foo(Y<String>.Z z, int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y<String>.Z::<String>foo;
			        i.foo(new Y<String>().new Z(), 10);\s
			    }
			}
			class Y<T> {
			    class Z {
			        void foo(int x) {
				    System.out.println(x);
			        }
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(Y<String>.Z z, int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y<String>.Z::<String>foo;
			    i.foo(new Y<String>().new Z(), 10);
			  }
			}
			class Y<T> {
			  class Z {
			    Z() {
			      super();
			    }
			    void foo(int x) {
			      System.out.println(x);
			    }
			  }
			  Y() {
			    super();
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0012", expectedUnitToString);
	}
	// Reference expression - X<T>.Y<K>:: form, without type arguments.
	public void test0013() throws IOException {
		String source =
				"""
			interface I {
			    void foo(Y<String>.Z<Integer> z, int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y<String>.Z<Integer>::foo;
			        i.foo(new Y<String>().new Z<Integer>(), 10);\s
			    }
			}
			class Y<T> {
			    class Z<K> {
			        void foo(int x) {
				    System.out.println(x);
			        }
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(Y<String>.Z<Integer> z, int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y<String>.Z<Integer>::foo;
			    i.foo(new Y<String>().new Z<Integer>(), 10);
			  }
			}
			class Y<T> {
			  class Z<K> {
			    Z() {
			      super();
			    }
			    void foo(int x) {
			      System.out.println(x);
			    }
			  }
			  Y() {
			    super();
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0013", expectedUnitToString);
	}
	// Reference expression - X<T>.Y<K>:: form, with type arguments.
	public void test0014() throws IOException {
		String source =
				"""
			interface I {
			    void foo(Y<String>.Z<Integer> z, int x);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y<String>.Z<Integer>::<String>foo;
			        i.foo(new Y<String>().new Z<Integer>(), 10);\s
			    }
			}
			class Y<T> {
			    class Z<K> {
			        void foo(int x) {
				    System.out.println(x);
			        }
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(Y<String>.Z<Integer> z, int x);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y<String>.Z<Integer>::<String>foo;
			    i.foo(new Y<String>().new Z<Integer>(), 10);
			  }
			}
			class Y<T> {
			  class Z<K> {
			    Z() {
			      super();
			    }
			    void foo(int x) {
			      System.out.println(x);
			    }
			  }
			  Y() {
			    super();
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0014", expectedUnitToString);
	}
	// Constructor reference expression - X<T>.Y<K>::new form, with type arguments.
	public void test0015() throws IOException {
		String source =
				"""
			interface I {
			    void foo(Y<String> y);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = Y<String>.Z<Integer>::<String>new;
			        i.foo(new Y<String>());\s
			    }
			}
			class Y<T> {
			    class Z<K> {
			        Z() {
			            System.out.println("Y<T>.Z<K>::new");
			        }
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(Y<String> y);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = Y<String>.Z<Integer>::<String>new;
			    i.foo(new Y<String>());
			  }
			}
			class Y<T> {
			  class Z<K> {
			    Z() {
			      super();
			      System.out.println("Y<T>.Z<K>::new");
			    }
			  }
			  Y() {
			    super();
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0015", expectedUnitToString);
	}
	// Reference expression - PrimitiveType[]:: form, with type arguments.
	public void test0016() throws IOException {
		String source =
				"""
			interface I {
			    Object copy(int [] ia);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = int[]::<String>clone;
			        i.copy(new int[10]);\s
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  Object copy(int[] ia);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = int[]::<String>clone;
			    i.copy(new int[10]);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0016", expectedUnitToString);
	}
	// Reference expression - Name[]:: form, with type arguments.
	public void test0017() throws IOException {
		String source =
				"""
			interface I {
			    Object copy(X [] ia);
			}
			public class X  {
			    public static void main(String [] args) {
			        I i = X[]::<String>clone;
			        i.copy(new X[10]);\s
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  Object copy(X[] ia);
			}
			public class X {
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = X[]::<String>clone;
			    i.copy(new X[10]);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0017", expectedUnitToString);
	}
	// Reference expression - X<T>.Y<K>[]:: form, with type arguments.
	public void test0018() throws IOException {
		String source =
				"""
			interface I {
			    Object copy(X<String>.Y<Integer> [] p);
			}
			public class X<T>  {
			    class Y<K> {
			    }
			    public static void main(String [] args) {
			        I i = X<String>.Y<Integer>[]::<String>clone;
			        X<String>.Y<Integer>[] xs = null;
			        i.copy(xs);\s
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  Object copy(X<String>.Y<Integer>[] p);
			}
			public class X<T> {
			  class Y<K> {
			    Y() {
			      super();
			    }
			  }
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    I i = X<String>.Y<Integer>[]::<String>clone;
			    X<String>.Y<Integer>[] xs = null;
			    i.copy(xs);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0018", expectedUnitToString);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=384320, syntax error while mixing 308 and 335.
	public void test0019() throws IOException {
		String source =
				"""
			interface I {
			    void foo(X<String> s, int x);
			}
			public class X<T> {
			    I i = X<@Foo({"hello"}) String>::foo;
			    void foo(int x) {
			    }
			}
			@interface Foo {
			    String [] value();
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  void foo(X<String> s, int x);
			}
			public class X<T> {
			  I i = X<@Foo({"hello"}) String>::foo;
			  public X() {
			    super();
			  }
			  void foo(int x) {
			  }
			}
			@interface Foo {
			  String[] value();
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0019", expectedUnitToString);
	}

	// Reference expression - Name::new forms, with/without type arguments.
	public void test0020() throws IOException {
		String source =
				"""
			interface I {
			    Y foo(int x);
			}
			public class X  {
			    class Z extends Y {
			        public Z(int x) {
			            super(x);
			            System.out.println("Z"+x);
			        }
			    }
			    public static void main(String [] args) {
			        Y y;
			        I i = Y::new;
			        y = i.foo(10);\s
			        i = X.Z::new;
			        y = i.foo(20);\s
			        i = W<Integer>::new;
			        y = i.foo(23);
			    }
			}
			class W<T> extends Y {
			    public W(T x) {
			        super(0);
			        System.out.println(x);
			    }
			}
			class Y {
			    public Y(int x) {
			        System.out.println(x);
			    }
			}
			""";
		String expectedUnitToString =
				"""
			interface I {
			  Y foo(int x);
			}
			public class X {
			  class Z extends Y {
			    public Z(int x) {
			      super(x);
			      System.out.println(("Z" + x));
			    }
			  }
			  public X() {
			    super();
			  }
			  public static void main(String[] args) {
			    Y y;
			    I i = Y::new;
			    y = i.foo(10);
			    i = X.Z::new;
			    y = i.foo(20);
			    i = W<Integer>::new;
			    y = i.foo(23);
			  }
			}
			class W<T> extends Y {
			  public W(T x) {
			    super(0);
			    System.out.println(x);
			  }
			}
			class Y {
			  public Y(int x) {
			    super();
			    System.out.println(x);
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0003", expectedUnitToString);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385132
	public void test385132() throws IOException {
		String source = "::";
		String expectedErrorString =
				"""
			----------
			1. ERROR in test385132 (at line 1)
				::
				^^
			Syntax error on token "::", delete this token
			----------
			""";

		checkParse(CHECK_PARSER , source.toCharArray(), expectedErrorString, "test385132", null);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385374, Support for 308 style type annotations on 335 constructs.
	public void test385374() throws IOException {
		String source =
				"""
			interface I {
				void foo();
			}
			@interface TypeAnnotation {
			}
			
			class X<T> {
				 // Primitive array form
				 I x1 = @TypeAnnotation int []::clone;
				 // Primitive array form with dimension annotations.
				 I x2 = @TypeAnnotation int @ArrayAnnotation[]@ArrayAnnotation[]::clone;\s
				 // Primitive array form with dimension annotations and type parameter annotations.
				 I x3 = @TypeAnnotation int @ArrayAnnotation[]@ArrayAnnotation[]::<@TypeParameterAnnotation String>clone;\s
				 // Reference type name form
				 I x4 = @TypeAnnotation X::clone;
				 // Reference type name array form
				 I x5 = @TypeAnnotation X []::clone;
				 // Reference type name array form with dimension annotations.
				 I x6 = @TypeAnnotation X @ArrayAnnotation[]@ArrayAnnotation[]::clone;\s
				 // Reference type name array form with dimension annotations and type parameter annotations.
				 I x7 = @TypeAnnotation X @ArrayAnnotation[]@ArrayAnnotation[]::<@TypeParameterAnnotation String>clone;\s
				 // Generic type array form with dimension annotations and type parameter annotations.
				 I x8 = @TypeAnnotation X<@TypeParameterAnnotation String> @ArrayAnnotation[]@ArrayAnnotation[]::<@TypeParameterAnnotation String>clone;\s
				 // Qualified generic type array form with dimension annotations and type parameter annotations.
				 I x9 = @TypeAnnotation X<@TypeParameterAnnotation String>.Y<@TypeParameterAnnotation String> @ArrayAnnotation[]@ArrayAnnotation[]::<@TypeParameterAnnotation String>clone;\s
			}
			""";

		String expectedUnitToString =
				"""
			interface I {
			  void foo();
			}
			@interface TypeAnnotation {
			}
			class X<T> {
			  I x1 = @TypeAnnotation int[]::clone;
			  I x2 = @TypeAnnotation int @ArrayAnnotation [] @ArrayAnnotation []::clone;
			  I x3 = @TypeAnnotation int @ArrayAnnotation [] @ArrayAnnotation []::<@TypeParameterAnnotation String>clone;
			  I x4 = @TypeAnnotation X::clone;
			  I x5 = @TypeAnnotation X[]::clone;
			  I x6 = @TypeAnnotation X @ArrayAnnotation [] @ArrayAnnotation []::clone;
			  I x7 = @TypeAnnotation X @ArrayAnnotation [] @ArrayAnnotation []::<@TypeParameterAnnotation String>clone;
			  I x8 = @TypeAnnotation X<@TypeParameterAnnotation String> @ArrayAnnotation [] @ArrayAnnotation []::<@TypeParameterAnnotation String>clone;
			  I x9 = @TypeAnnotation X<@TypeParameterAnnotation String>.Y<@TypeParameterAnnotation String> @ArrayAnnotation [] @ArrayAnnotation []::<@TypeParameterAnnotation String>clone;
			  X() {
			    super();
			  }
			}
			""";
		checkParse(CHECK_PARSER | CHECK_JAVAC_PARSER , source.toCharArray(), null, "test385374", expectedUnitToString);
	}
	/* https://bugs.eclipse.org/bugs/show_bug.cgi?id=385374, Support for 308 style type annotations on 335 constructs - make sure illegal modifiers are rejected
	   This test has been rendered meaningless as the grammar has been so throughly changed - Type annotations are not accepted via modifiers in the first place.
	   Disabling this test as we don't want fragile and unstable tests that are at the whimsy of the diagnose parser's complex algorithms.
	*/
	public void test385374a() throws IOException {
		// Nop.
	}
}
