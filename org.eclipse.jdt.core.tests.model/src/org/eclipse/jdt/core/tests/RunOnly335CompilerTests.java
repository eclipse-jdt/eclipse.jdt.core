/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.compiler.regression.*;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunOnly335CompilerTests extends TestCase {

	public RunOnly335CompilerTests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			GenericsRegressionTest_1_8.class,
			LambdaExpressionsTest.class,
			LambdaRegressionTest.class,
			NegativeLambdaExpressionsTest.class,
			OverloadResolutionTest8.class,
			LambdaShapeTests.class,
			NullTypeAnnotationTest.class, // tests type inference
		};
	}

	public static Class[] getCompilerClasses() {
		return new Class[] {
			GenericTypeTest.class,
			GenericsRegressionTest.class,
			GenericsRegressionTest_1_7.class,
		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunOnly335CompilerTests.class.getName());

		Class[] testClasses = getAllTestClasses();
		addTestsToSuite(ts, testClasses);

		AbstractCompilerTest.setpossibleComplianceLevels(AbstractCompilerTest.F_1_8);
		addTestsToSuite(ts, getCompilerClasses());
		return ts;
	}
	public static void addTestsToSuite(TestSuite suite, Class[] testClasses) {

		for (int i = 0; i < testClasses.length; i++) {
			Class testClass = testClasses[i];
			// call the suite() method and add the resulting suite to the suite
			try {
				Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
				Test test = (Test)suiteMethod.invoke(null, new Object[0]);
				suite.addTest(test);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}
	protected void tearDown() throws Exception {
		ConverterTestSetup.PROJECT_SETUP = false;
		super.tearDown();
	}
}
