/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Guards against super-linear blow-up of JLS 18 type inference.
 * <p>
 * The test compiles a single generic invocation that takes a large number of generic
 * invocations as its arguments, so that one inference context has to resolve thousands of
 * mutually dependent inference variables. It then asserts that this stays within a time
 * budget. The budget is deliberately very generous and is scaled with the speed of the
 * machine, which is derived from compiling a reference construct that exercises the same
 * inference code (a small variant of the same construct, resp. the construct of issue 5441).
 * </p>
 * <p>
 * Reference measurements (n=500): ~0.4s with the fix for
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5441, ~16s without it.
 * With a parameterized target type (n=400): ~1.1s with the fix for
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5624 (memoized incorporation of proper types
 * into parameterized dependencies), ~6.5s without it, i.e. ~3x resp. ~16x the time for the
 * construct of issue 5441 at n=500. Note that a
 * plain time <em>ratio</em> between two sizes is not usable here, because the constant
 * per-compilation overhead (a few tens of milliseconds) varies with the JIT state and would
 * dominate the small measurement.
 * </p>
 */
@SuppressWarnings({ "rawtypes" })
public class InferenceScalabilityTest extends AbstractRegressionTest {

	/** Number of poly-expression arguments used to calibrate the speed of the machine. */
	private static final int SMALL = 50;
	/** Number of poly-expression arguments under test - the size reported in issue 5441. */
	private static final int LARGE = 500;
	/** Size for {@link #testIssue5624_manyNestedGenericInvocationsWithParameterizedTarget()}. */
	private static final int LARGE_PARAMETERIZED = 400;
	/** Budget for LARGE, relative to SMALL; only relevant on slow machines. */
	private static final long RELATIVE_BUDGET = 60;
	/**
	 * Budget for LARGE_PARAMETERIZED, relative to LARGE (of the 5441 construct). Measured ratios are ~3 with
	 * the fix for issue 5624 and ~15-18 without it, independently of CPU speed and number of cores.
	 */
	private static final long RELATIVE_BUDGET_PARAMETERIZED = 8;
	/** Budget for LARGE on a fast machine, in milliseconds. */
	private static final long MINIMUM_BUDGET_MS = 4000;

	public InferenceScalabilityTest(String name) {
		super(name);
	}

	public static Class testClass() {
		return InferenceScalabilityTest.class;
	}

	public static Test suite() {
		// a pure timing guard - running it once is enough, no need to repeat per compliance level
		return buildUniqueComplianceTestSuite(testClass(), CompilerOptions.getFirstSupportedJdkLevel());
	}

	/**
	 * A single generic invocation whose arguments are all generic invocations themselves,
	 * i.e. one inference context holding 2 + 2*count mutually dependent inference variables.
	 * Deliberately uses only self-declared API, so that the test is independent of the JRE it
	 * happens to run on.
	 */
	private String sourceWithNestedGenericInvocations(int count) {
		StringBuilder buf = new StringBuilder(count * 32);
		buf.append("import java.util.Map;\n");
		buf.append("public class X {\n");
		buf.append("	static class Entry<K,V> {}\n");
		buf.append("	static <K,V> Entry<K,V> entry(K k, V v) { return null; }\n");
		buf.append("	@SafeVarargs\n");
		buf.append("	static <K,V> Map<K,V> ofEntries(Entry<? extends K, ? extends V>... entries) { return null; }\n");
		buf.append("	static final Map<String,String> MAP = ofEntries(\n");
		for (int i = 0; i < count; i++) {
			buf.append("		entry(\"k").append(i).append("\", \"v").append(i).append("\")");
			buf.append(i < count - 1 ? ",\n" : "\n");
		}
		buf.append("	);\n");
		buf.append("}\n");
		return buf.toString();
	}

	/**
	 * Like {@link #sourceWithNestedGenericInvocations(int)}, but the outer invocation targets a
	 * parameterized type, so that its inference variable depends on a parameterized type mentioning
	 * further inference variables ({@code T <: Pair<A,B>}), cf. JDK-8221301.
	 */
	private String sourceWithNestedGenericInvocationsAndParameterizedTarget(int count) {
		StringBuilder buf = new StringBuilder(count * 32);
		buf.append("public class X {\n");
		buf.append("	static class Pair<A,B> {\n");
		buf.append("		static <A,B> Pair<A,B> of(A a, B b) { return null; }\n");
		buf.append("	}\n");
		buf.append("	static class List<T> {\n");
		buf.append("		@SafeVarargs\n");
		buf.append("		static <T> List<T> of(T... elements) { return null; }\n");
		buf.append("	}\n");
		buf.append("	static final List<Pair<String,String>> PAIRS = List.of(\n");
		for (int i = 0; i < count; i++) {
			buf.append("		Pair.of(\"a").append(i).append("\", \"b").append(i).append("\")");
			buf.append(i < count - 1 ? ",\n" : "\n");
		}
		buf.append("	);\n");
		buf.append("}\n");
		return buf.toString();
	}

	private long timeCompilationMillis(String source) {
		String[] testFiles = new String[] { "X.java", source };
		long start = System.nanoTime();
		runConformTest(testFiles);
		return (System.nanoTime() - start) / 1_000_000L;
	}

	/**
	 * Asserts that compiling {@code source} takes at most {@code relativeBudget} times as long as compiling
	 * {@code referenceSource} (but is always granted {@link #MINIMUM_BUDGET_MS}). The reference is used to
	 * calibrate the speed of the machine, it is compiled first to warm up the JIT, then measured as best of three.
	 */
	private void assertWithinBudget(String what, String source, String referenceWhat, String referenceSource,
			long relativeBudget) {
		if (RUN_JAVAC || RUN_JAVAC_OPT_IN) {
			return; // comparing against javac would dominate and distort the measurement
		}

		for (int i = 0; i < 2; i++) {
			timeCompilationMillis(referenceSource); // warm up JIT, JCL name environment and output directory
		}

		long reference = Long.MAX_VALUE;
		for (int i = 0; i < 3; i++) {
			reference = Math.min(reference, timeCompilationMillis(referenceSource));
		}
		long budget = Math.max(MINIMUM_BUDGET_MS, relativeBudget * reference);

		long actual = timeCompilationMillis(source);
		if (actual > budget) { // the machine may just have been busy, give it one more chance
			actual = Math.min(actual, timeCompilationMillis(source));
		}

		String measurement = "compiling " + referenceWhat + " took " + reference
				+ "ms, compiling " + what + " took " + actual + "ms, budget was " + budget + "ms";
		System.out.println(testClass().getSimpleName() + "." + getName() + ": " + measurement);
		assertTrue("Type inference does not scale: " + measurement, actual <= budget);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5441
	// inference must not degrade super-linearly with the number of poly arguments
	public void testIssue5441_manyNestedGenericInvocations() {
		assertWithinBudget(
				LARGE + " nested generic invocations", sourceWithNestedGenericInvocations(LARGE),
				SMALL + " nested generic invocations", sourceWithNestedGenericInvocations(SMALL),
				RELATIVE_BUDGET);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5624, cf. JDK-8221301
	// incorporating proper types into parameterized dependencies must not be repeated for each pair of bounds
	public void testIssue5624_manyNestedGenericInvocationsWithParameterizedTarget() {
		// Calibrated against the construct of issue 5441 at full size: it exercises the very same inference
		// code (and is not affected by the fix for 5624), so slow CPUs, few cores or a small heap affect both
		// measurements alike. A small variant of the construct itself is not usable as reference, because its
		// compile time is dominated by constant overhead that does not slow down in proportion on a slow machine.
		assertWithinBudget(
				LARGE_PARAMETERIZED + " nested generic invocations with parameterized target",
				sourceWithNestedGenericInvocationsAndParameterizedTarget(LARGE_PARAMETERIZED),
				LARGE + " nested generic invocations", sourceWithNestedGenericInvocations(LARGE),
				RELATIVE_BUDGET_PARAMETERIZED);
	}
}