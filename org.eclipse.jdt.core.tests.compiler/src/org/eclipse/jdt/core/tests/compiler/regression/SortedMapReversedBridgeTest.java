/*******************************************************************************
 * Copyright (c) 2026 Eclipse contributors and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     See git history
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;

/**
 * Reproduces a bug where, when compiling with -source/-target 8 while running
 * on a JDK 21+ (or later) host without an explicit --release/bootclasspath
 * restriction, ECJ synthesizes a bridge method for the new
 * java.util.SequencedMap#reversed() abstract method on any class that
 * (transitively) implements java.util.SortedMap, even though that class does
 * not override reversed() itself.
 *
 * The synthesized bridge references java.util.SequencedMap, a type that does
 * not exist before JDK 21, making the resulting Java-8-format class file
 * (major version 52) fail with a ClassNotFoundException at runtime on any
 * JDK 8-20 VM the moment reversed() is invoked (or, on some VMs, at class
 * verification time).
 *
 * javac, given the exact same "-source 8 -target 8" flags on the same JDK 21
 * host, does NOT synthesize this bridge, which shows the behavior is an ECJ
 * inconsistency, not merely an unavoidable consequence of "-source 8 without
 * --release/bootclasspath" on a new JDK.
 *
 * Real-world case that triggered this: org.apache.felix.resolver.util.OpenHashMap
 * (vendored in Eclipse Equinox's org.eclipse.osgi), which implements SortedMap
 * without overriding reversed().
 */
public class SortedMapReversedBridgeTest extends AbstractRegressionTest {

	public SortedMapReversedBridgeTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(SortedMapReversedBridgeTest.class, F_1_8);
	}

	// A minimal stand-in for org.apache.felix.resolver.util.OpenHashMap: a
	// concrete class implementing SortedMap without declaring reversed().
	private static final String[] TEST_SOURCE = new String[] {
		"MyMap.java",
		"import java.util.AbstractMap;\n" +
		"import java.util.Comparator;\n" +
		"import java.util.SortedMap;\n" +
		"import java.util.Set;\n" +
		"public class MyMap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V> {\n" +
		"	public Comparator<? super K> comparator() { return null; }\n" +
		"	public SortedMap<K, V> subMap(K fromKey, K toKey) { return null; }\n" +
		"	public SortedMap<K, V> headMap(K toKey) { return null; }\n" +
		"	public SortedMap<K, V> tailMap(K fromKey) { return null; }\n" +
		"	public K firstKey() { return null; }\n" +
		"	public K lastKey() { return null; }\n" +
		"	public Set<Entry<K, V>> entrySet() { return null; }\n" +
		"}\n"
	};

	/**
	 * Compiles the minimal SortedMap implementer at -source/-target 8 and
	 * asserts the resulting class file does NOT reference java.util.SequencedMap.
	 * This currently FAILS on a JDK 21+ compiler host, demonstrating the bug.
	 */
	public void test001_noSequencedMapBridgeAtSource8() throws IOException, ClassFormatException {
		this.runConformTest(TEST_SOURCE);

		File classFile = new File(OUTPUT_DIR + File.separator + "MyMap.class");
		byte[] classFileBytes = Util.getFileByteContent(classFile);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);

		if (actualOutput.contains("SequencedMap")) {
			fail("MyMap.class compiled at -source/-target 8 must not reference java.util.SequencedMap, "
				+ "otherwise it is unusable on any JDK < 21 (ClassNotFoundException at runtime). "
				+ "Disassembly was:\n" + actualOutput);
		}
	}
}
