/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Arrays;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.compiler.regression.SwitchExpressionsYieldTest;
import org.eclipse.jdt.core.tests.dom.ASTConverter14Test;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.model.JavaSearchBugs13Tests;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingTest;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunOnlyJava13Tests extends TestCase {

	public RunOnlyJava13Tests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			JavaSearchBugs13Tests.class,
			SwitchExpressionsYieldTest.class,
		};
	}

	public static Class[] getConverterTestClasses() {
		return new Class[] {
				ASTConverter14Test.class,
				ASTRewritingTest.class,
		};
	}
	public static Test suite() {
		TestSuite ts = new TestSuite(RunOnlyJava13Tests.class.getName());

		Class[] testClasses = getAllTestClasses();
		addTestsToSuite(ts, testClasses);
		testClasses = getConverterTestClasses();
		ConverterTestSetup.TEST_SUITES = new ArrayList(Arrays.asList(testClasses));
		addTestsToSuite(ts, testClasses);
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
