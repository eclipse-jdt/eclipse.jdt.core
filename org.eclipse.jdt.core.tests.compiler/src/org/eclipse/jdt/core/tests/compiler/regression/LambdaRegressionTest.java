/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;
public class LambdaRegressionTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test001"};
//	TESTS_NUMBERS = new int[] { 50 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public LambdaRegressionTest(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_8);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes 
public void test001() {
	this.runConformTest(
			new String[] {
				"X.java",
				"import java.util.HashMap;\n" +
				"import java.util.Map;\n" +
				"import java.util.function.Function;\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().run();\n" +
				"  }\n" +
				"  public void run() {\n" +
				"    class Inner {\n" +
				"      public Inner() {\n" +
				"        System.out.println(\"miep\");\n" +
				"      }\n" +
				"    }\n" +
				"    Map<String, Inner> map = new HashMap<>();\n" +
				"    Function<String, Inner> function = (name) -> {\n" +
				"      Inner i = map.get(name);\n" +
				"      if (i == null) {\n" +
				"        i = new Inner();\n" +
				"        map.put(name, i);\n" +
				"      }\n" +
				"      return i;\n" +
				"\n" +
				"    };\n" +
				"    function.apply(\"test\");\n" +
				"  }\n" +
				"}\n",
			},
			"miep"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes 
public void test002() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"@SuppressWarnings(\"all\")\n" +
				"public class X {\n" +
				"  private final String text = \"Bug?\";\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doIt();\n" +
				"  }\n" +
				"  private void doIt() {\n" +
				"    new Sub();\n" +
				"  }\n" +
				"  private class Super<T> {\n" +
				"    public Super(Consumer<T> consumer) {\n" +
				"    }\n" +
				"  }\n" +
				"  private class Sub extends Super<String> {\n" +
				"    public Sub() {\n" +
				"      super(s -> System.out.println(text));\n" +
				"      // super(s -> System.out.println(\"miep\"));\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 17)\n" + 
			"	super(s -> System.out.println(text));\n" + 
			"	      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Cannot refer to \'this\' nor \'super\' while explicitly invoking a constructor\n" + 
			"----------\n"
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes 
public void test003() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.function.Consumer;\n" +
				"@SuppressWarnings(\"all\")\n" +
				"public class X {\n" +
				"  private final String text = \"Bug?\";\n" +
				"  public static void main(String[] args) {\n" +
				"    new X().doIt();\n" +
				"  }\n" +
				"  private void doIt() {\n" +
				"    new Sub();\n" +
				"  }\n" +
				"  private class Super<T> {\n" +
				"    public Super(Consumer<T> consumer) {\n" +
				"    }\n" +
				"  }\n" +
				"  private class Sub extends Super<String> {\n" +
				"    public Sub() {\n" +
				"       super(s -> System.out.println(\"miep\"));\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
			},
			""
			);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446317, java.lang.VerifyError: Bad type on operand stack with Lambdas and/or inner classes
public void test004() {
	this.runConformTest(
		new String[] {
			"Y.java", 
			"import java.util.function.Supplier;\n" + 
			"class E {\n" + 
			"	E(Supplier<Object> factory) { }\n" + 
			"}\n" + 
			"public class Y extends E {\n" + 
			"	Y() {\n" + 
			"		super( () -> {\n" + 
			"			class Z extends E {\n" + 
			"				Z() {\n" + 
			"					super(() -> new Object());\n" + 
			"				}\n" + 
			"			}\n" + 
			"			return null;\n" + 
			"			});\n" + 
			"	}\n" + 
			"	public static void main(String[] args) {\n" + 
			"		new Y();\n" + 
			"	}\n" + 
			"}"
	});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448724, [1.8] [compiler] Wrong resolution of overloaded method when irrelevant type parameter is present and lambda is used as parameter
public void test448724() {
	this.runConformTest(
		new String[] {
			"X.java", 
			"import java.util.concurrent.Callable;\n" +
			"public class X {\n" +
			"	public void mismatchRunnableCallable() throws Exception {\n" +
			"		//Resolves to case1(Runnable) method invocation; lambda with block\n" +
			"		case1(() -> {\"abc\".length();});\n" +
			"		//Resolves to case1(Callable) method invocation, resulting in type mismatch; block removed - lambda with expression\n" +
			"                case1(() -> \"abc\".length());\n" +
			"	}\n" +
			"	public void noSuchMismatch() throws Exception {\n" +
			"		//no difference to case1 \n" +
			"		case2(() -> {\"abc\".length();});\n" +
			"		//the only difference to case 1 is the missing irrelevant <T> type parameter. Properly resolves to case2(Runnable) here\n" +
			"		case2(() -> \"abc\".length());\n" +
			"	}\n" +
			"	public void case1(final Runnable r) {\n" +
			"		System.out.println(\"case1: Runnable\");\n" +
			"	}\n" +
			"	public <T> void case1(Callable<Boolean> c) {\n" +
			"		System.out.println(\"case1: Callable\");\n" +
			"	}\n" +
			"	public void case2(final Runnable supplier) {\n" +
			"		System.out.println(\"case2: Runnable\");\n" +
			"	}\n" +
			"	public void case2(Callable<Boolean> conditionEvaluator) {\n" +
			"		System.out.println(\"case2: Callable\");\n" +
			"	}\n" +
			"	public static void main(String[] args) throws Exception {\n" +
			"		new X().mismatchRunnableCallable();\n" +
			"		new X().noSuchMismatch();\n" +
			"	}\n" +
			"}\n"
	},
	"case1: Runnable\n" + 
	"case1: Runnable\n" + 
	"case2: Runnable\n" + 
	"case2: Runnable");
}
public static Class testClass() {
	return LambdaRegressionTest.class;
}
}
