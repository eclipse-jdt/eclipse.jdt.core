/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.regression.*;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.model.JavaSearchBugs14SwitchExpressionTests;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingSwitchExpressionsTest;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingSwitchPatternTest;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunVariousSwitchTests extends TestCase {

	public RunVariousSwitchTests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {

				SwitchPatternTest.class,
				SwitchPatternTest22.class,
				SwitchTest.class,
				SwitchExpressionsYieldTest.class,

				RecordPatternTest.class,
				InstanceofPrimaryPatternTest.class,
				PatternMatching16Test.class,
				UnnamedPatternsAndVariablesTest.class,
				JEP441SnippetsTest.class,
				FlowAnalysisTest.class,
				EnumTest.class,
				LocalEnumTest.class,
				ConstantTest.class,

				JavaSearchBugs14SwitchExpressionTests.class,
				ASTRewritingSwitchExpressionsTest.class,
				ASTRewritingSwitchPatternTest.class,
				ComplianceDiagnoseTest.class,

		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunVariousSwitchTests.class.getName());

		Class[] testClasses = getAllTestClasses();
		addTestsToSuite(ts, testClasses);

		AbstractCompilerTest.setpossibleComplianceLevels(AbstractCompilerTest.F_1_8);

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