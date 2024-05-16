/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation.
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

import java.io.File;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class BatchCompilerTest_14 extends AbstractBatchCompilerTest {

	static {
//		TESTS_NAMES = new String[] { "testBatchBug565787_001" };
//		TESTS_NUMBERS = new int[] { 306 };
//		TESTS_RANGE = new int[] { 298, -1 };
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_14);
	}
	public static Class testClass() {
		return BatchCompilerTest_14.class;
	}
	public BatchCompilerTest_14(String name) {
		super(name);
	}
public void testBatchBug565787_001() throws Exception {
	this.runConformTest(
			new String[] {
					"X.java",
					"""
						import java.util.Arrays;
						public class X {
						       class MR {
						               public int mrCompare(String str1, String str2) {
						                       return 0;
						               }
						       };
						      \s
						       void m1() {
						               MR mr = new MR();
						               String[] array = {"one"};
						               Arrays.sort(array, mr::mrCompare);
						       }
						}
						""",
					"Y.java",
					"""
						import java.util.HashSet;
						import java.util.function.Supplier;
						public class Y {
						       class MR {
						               public <T> void mr(Supplier<T> supplier) {}
						       };
						      \s
						       void m1() {
						               MR mr = new MR();
						               mr.mr(HashSet<String>::new);
						       }
						}
						"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "Y.java\""
			+" \"" + OUTPUT_DIR +  File.separator + "X.java\""
			+ " -source " + CompilerOptions.getLatestVersion(),
					"",
					"",
					true);
	String expectedOutput = """
		Bootstrap methods:
		  0 : # 44 invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
			Method arguments:
				#46 (Ljava/lang/Object;Ljava/lang/Object;)I
				#51 X$MR.mrCompare:(Ljava/lang/String;Ljava/lang/String;)I
				#52 (Ljava/lang/String;Ljava/lang/String;)I
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
}
