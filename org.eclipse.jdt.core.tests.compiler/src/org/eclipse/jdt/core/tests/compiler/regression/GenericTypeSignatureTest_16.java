/*******************************************************************************
 * Copyright (c) 2023 coehlrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

@SuppressWarnings({ "rawtypes" })
public class GenericTypeSignatureTest_16 extends AbstractRegressionTest {

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}

	public static Class testClass() {
		return GenericTypeSignatureTest_16.class;
	}

	public GenericTypeSignatureTest_16(String name) {
		super(name);
	}

	/**
	 */
	protected void cleanUp() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
	}

	public void test799() {
		final String[] testsSource = new String[] {
			"X.java",
			"public record X(Y<String> y) {\n" +
			"  public X() {\n" +
			"    this(null);\n" +
			"  }\n" +
			"}\n",
			"Y.java",
			"public class Y<T> {}\n"
		};

		runConformTest(testsSource);

		try {
			ClassFileReader classFileReader = ClassFileReader.read(OUTPUT_DIR + File.separator + "X.class");
			IBinaryMethod[] methods = classFileReader.getMethods();
			assertNotNull("No methods", methods);
			boolean found = false;
			for (IBinaryMethod method : methods) {
				if ("<init>".equals(new String(method.getSelector())) && "(LY;)V".equals(new String(method.getMethodDescriptor()))) {
					found = true;

					char[] signature = method.getGenericSignature();
					assertNotNull("No signature", signature);
					assertEquals("Wrong signature", "(LY<Ljava/lang/String;>;)V", new String(signature));
				}
			}
			assertTrue("No canonical constructor", found);
		} catch (ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}

}
