/*******************************************************************************
* Copyright (c) 2023 Advantest Europe GmbH and others.
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

import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.parser.PatternMatchingSelectionTest;
import org.eclipse.jdt.core.tests.compiler.regression.InstanceofExpressionTest;
import org.eclipse.jdt.core.tests.compiler.regression.InstanceofPrimaryPatternTest;
import org.eclipse.jdt.core.tests.compiler.regression.NullAnnotationTests21;
import org.eclipse.jdt.core.tests.compiler.regression.PatternMatching16Test;
import org.eclipse.jdt.core.tests.compiler.regression.RecordPatternProjectTest;
import org.eclipse.jdt.core.tests.compiler.regression.RecordPatternTest;
import org.eclipse.jdt.core.tests.compiler.regression.SwitchExpressionsYieldTest;
import org.eclipse.jdt.core.tests.compiler.regression.SwitchPatternTest;
import org.eclipse.jdt.core.tests.compiler.regression.SwitchPatternTest21;
import org.eclipse.jdt.core.tests.compiler.regression.UnnamedPatternsAndVariablesTest;
import org.eclipse.jdt.core.tests.dom.ASTConverter_GuardedPattern_Test;
import org.eclipse.jdt.core.tests.dom.ASTConverter_RecordPattern_Test;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.model.CompletionTestsForRecordPattern;
import org.eclipse.jdt.core.tests.model.JavaSearchBugs19Tests;
import org.eclipse.jdt.core.tests.model.ResolveTests12To15;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingInstanceOfPatternExpressionTest;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingRecordPatternTest;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingSwitchPatternTest;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunVariousPatternsTests extends TestCase {

	public RunVariousPatternsTests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
				RecordPatternTest.class,
				RecordPatternProjectTest.class,
				SwitchPatternTest.class,
				InstanceofPrimaryPatternTest.class,
				PatternMatching16Test.class,
				SwitchExpressionsYieldTest.class,
				ASTConverter_RecordPattern_Test.class,
				PatternMatchingSelectionTest.class,
				UnnamedPatternsAndVariablesTest.class,
				ASTConverter_GuardedPattern_Test.class,
				ASTRewritingRecordPatternTest.class,
				ASTRewritingInstanceOfPatternExpressionTest.class,
				ASTRewritingSwitchPatternTest.class,
				ResolveTests12To15.class,
				SwitchPatternTest21.class,
				JavaSearchBugs19Tests.class,
				CompletionTestsForRecordPattern.class,
				NullAnnotationTests21.class,
				ComplianceDiagnoseTest.class,
				InstanceofExpressionTest.class,
		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunVariousPatternsTests.class.getName());

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
