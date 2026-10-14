/*******************************************************************************
* Copyright (c) 2026 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
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
import org.eclipse.jdt.core.tests.builder.IncrementalTests28;
import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.regression.CompilerInvocationTests;
import org.eclipse.jdt.core.tests.compiler.regression.CopilotGeneratedJep401Tests;
import org.eclipse.jdt.core.tests.compiler.regression.EarlyConstructionContextTests28;
import org.eclipse.jdt.core.tests.compiler.regression.RecordsRestrictedClassTest;
import org.eclipse.jdt.core.tests.compiler.regression.RecordsWithStrictFieldsTest;
import org.eclipse.jdt.core.tests.compiler.regression.SuperAfterStatementsTest;
import org.eclipse.jdt.core.tests.compiler.regression.ValueClassesAndObjectsTest;
import org.eclipse.jdt.core.tests.dom.ASTConverterBugsTest;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.model.CompletionTests28;
import org.eclipse.jdt.core.tests.model.JavaSearchTests;


@SuppressWarnings({"rawtypes", "unchecked"})
public class RunOnlyJava28Tests extends TestCase {

	public RunOnlyJava28Tests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			ComplianceDiagnoseTest.class,
			CompilerInvocationTests.class,
			ValueClassesAndObjectsTest.class,
			EarlyConstructionContextTests28.class,
			SuperAfterStatementsTest.class,
			RecordsWithStrictFieldsTest.class,
			CopilotGeneratedJep401Tests.class,
			RecordsRestrictedClassTest.class,
			CompletionTests28.class,
			JavaSearchTests.class,
			ASTConverterBugsTest.class,
			IncrementalTests28.class,
		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunOnlyJava28Tests.class.getName());
		Class[] testClasses = getAllTestClasses();
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