/*******************************************************************************
 * Copyright (c) 2011, 2018 IBM Corporation.
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

import junit.framework.Test;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.JavacHasABug;

@SuppressWarnings({ "rawtypes" })
public class PolymorphicSignatureTest extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "testBug515863" };
	}
	public PolymorphicSignatureTest(String name) {
		super(name);
	}

	// ========= OPT-IN to run.javac mode: ===========
	@Override
	protected void setUp() throws Exception {
		this.runJavacOptIn = true;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.runJavacOptIn = false; // do it last, so super can still clean up
	}
	// =================================================

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), FIRST_SUPPORTED_JAVA_VERSION);
	}
	public static Class testClass() {
		return PolymorphicSignatureTest.class;
	}

	public void test0001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.*;\n" +
				"public class X {\n" +
				"   public static void main(String[] args) throws Throwable{\n" +
				"      MethodType mt; MethodHandle mh; \n" +
				"      MethodHandles.Lookup lookup = MethodHandles.lookup();\n" +
				"      mt = MethodType.methodType(String.class, char.class, char.class);\n"+
				"      mh = lookup.findVirtual(String.class, \"replace\", mt);\n"+
				"      String s = (String) mh.invokeExact(\"daddy\",'d','n');\n"+
				"      System.out.println(s);\n"+
				"   }\n" +
				"}\n"
			},
			"nanny");
	}
	public void test0002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"import static java.lang.invoke.MethodHandles.*; \n" +
				"import java.lang.invoke.MethodHandle;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) throws Throwable {\n" +
				"		MethodHandle mh = dropArguments(insertArguments(identity(int.class), 0, 42), 0, Object[].class);\n" +
				"		int value = (int)mh.invokeExact(new Object[0]);\n" +
				"		System.out.println(value);\n"+
				"	}\n" +
				"}"
			},
			"42");
	}
	public void testBug515863() {
		runConformTest(
			new String[] {
				"Test.java",
				"import java.lang.invoke.MethodHandle;\n" +
				"import java.util.ArrayList;\n" +
				"import java.util.Collections;\n" +
				"\n" +
				"public class Test {\n" +
				"	\n" +
				"	public void foo() throws Throwable {\n" +
				"		\n" +
				"		MethodHandle mh = null;\n" +
				"		mh.invoke(null);                           // works, no issues.\n" +
				"		mh.invoke(null, new ArrayList<>());        // Bug 501457 fixed this\n" +
				"		mh.invoke(null, Collections.emptyList());  // This triggers UOE\n" +
				"		\n" +
				"	}\n" +
				"}\n"
			});
	}
	public void testBug475996() {
		if (!isJRE9Plus)
			return; // VarHandle is @since 9
		runConformTest(
			new String[] {
				"X.java",
				"import java.lang.invoke.VarHandle;\n" +
				"public class X<T> {\n" +
				"	static class Token {}\n" +
				"	Token NIL = new Token();\n" +
				"	VarHandle RESULT;\n" +
				"	void call(T t) {\n" +
				"		RESULT.compareAndSet(this, null, (t==null) ? NIL : t);\n" +
				"	}\n" +
				"" +
				"}\n"
			});
	}
	public void testGH3651() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"VarHandleCast.java",
				"""
				import java.lang.invoke.VarHandle;
				class VarHandleCast<V> {
				     VarHandle vh;
				     V method(Object obj) {
				         return (V)vh.getAndSet(this, obj);
				     }
				}
				"""
			};
		runner.expectedCompilerLog = """
			----------
			1. WARNING in VarHandleCast.java (at line 5)
				return (V)vh.getAndSet(this, obj);
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Object to V
			----------
			""";
		runner.javacTestOptions = JavacHasABug.JavacBug8343286;
		runner.runWarningTest();
	}
}
