/*******************************************************************************
 * Copyright (c) 2016, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.jdt.core.tests.dom.ASTConverter9Test;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.model.CompletionTests9;
import org.eclipse.jdt.core.tests.model.Java9ElementTests;
import org.eclipse.jdt.core.tests.model.JavaConventionTests;
import org.eclipse.jdt.core.tests.model.JavaSearchBugs9Tests;
import org.eclipse.jdt.core.tests.model.ModuleBuilderTests;
import org.eclipse.jdt.core.tests.model.ModuleOptionsTests;
import org.eclipse.jdt.core.tests.model.ResolveTests9;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunAllJava9Tests extends TestCase {

	public RunAllJava9Tests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			ModuleBuilderTests.class,
			ModuleOptionsTests.class,
			Java9ElementTests.class,
			JavaSearchBugs9Tests.class,
			CompletionTests9.class,
			ResolveTests9.class,
			JavaConventionTests.class
		};
	}

	public static Class[] getConverterTestClasses() {
		return new Class[] {
				ASTConverter9Test.class,
		};
	}

	public static Class[] getCompilerClasses() {
		return new Class[] {
			org.eclipse.jdt.core.tests.eval.TestAll.class,
			org.eclipse.jdt.core.tests.compiler.regression.TestAll.class,
		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunAllJava9Tests.class.getName());

		Class[] testClasses = getAllTestClasses();
		addTestsToSuite(ts, testClasses);
		testClasses = getConverterTestClasses();
		addTestsToSuite(ts, testClasses);

		AbstractCompilerTest.setpossibleComplianceLevels(AbstractCompilerTest.F_9);
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
