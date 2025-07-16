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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.regression.SwitchExpressionsYieldTest;
import org.eclipse.jdt.core.tests.dom.ASTConverter14Test;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.model.JavaSearchBugs13Tests;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingTest;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunAllJava13Tests extends TestCase {

	public RunAllJava13Tests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			JavaSearchBugs13Tests.class,
			ComplianceDiagnoseTest.class,
			SwitchExpressionsYieldTest.class,
			org.eclipse.jdt.compiler.apt.tests.AllAptTests.class,
		};
	}

	public static Class[] getConverterTestClasses() {
		return new Class[] {
				ASTConverter14Test.class,
				ASTRewritingTest.class,
		};
	}

	public static Class[] getCompilerClasses() {
		return new Class[] {
			org.eclipse.jdt.core.tests.eval.TestAll.class,
			org.eclipse.jdt.core.tests.compiler.regression.TestAll.class,
		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunAllJava13Tests.class.getName());

		Class[] testClasses = getAllTestClasses();
		addTestsToSuite(ts, testClasses);
		testClasses = getConverterTestClasses();
		addTestsToSuite(ts, testClasses);

		AbstractCompilerTest.setpossibleComplianceLevels(AbstractCompilerTest.F_13);
		addTestsToSuite(ts, getCompilerClasses());
		// ComplianceDiagnoseTest is already added to the test suite through getTestSuite
		ts.addTest(org.eclipse.jdt.core.tests.compiler.parser.TestAll.getTestSuite(false));
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