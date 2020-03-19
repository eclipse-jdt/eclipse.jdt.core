/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.builder.IncrementalTests18;
import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.dom.ASTConverter15JLS8Test;
import org.eclipse.jdt.core.tests.dom.ASTConverter17Test;
import org.eclipse.jdt.core.tests.dom.ASTConverter18Test;
import org.eclipse.jdt.core.tests.dom.ASTConverterAST8Test;
import org.eclipse.jdt.core.tests.dom.ASTConverterBugsTestJLS8;
import org.eclipse.jdt.core.tests.dom.ASTConverterTestAST8_2;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.dom.TypeAnnotationsConverterTest;
import org.eclipse.jdt.core.tests.dom.TypeBindingTests308;
import org.eclipse.jdt.core.tests.formatter.FormatterBugs18Tests;
import org.eclipse.jdt.core.tests.formatter.FormatterJSR308Tests;
import org.eclipse.jdt.core.tests.formatter.FormatterJSR335Tests;
import org.eclipse.jdt.core.tests.model.CompletionTests18;
import org.eclipse.jdt.core.tests.model.JavaElement8Tests;
import org.eclipse.jdt.core.tests.model.JavaSearchBugs8Tests;
import org.eclipse.jdt.core.tests.model.ResolveTests18;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingTest;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunAllJava8Tests extends TestCase {

	public RunAllJava8Tests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			ComplianceDiagnoseTest.class,
			FormatterJSR335Tests.class,
			FormatterJSR308Tests.class,
			FormatterBugs18Tests.class,
			JavaSearchBugs8Tests.class,
			ResolveTests18.class,
			CompletionTests18.class,
			IncrementalTests18.class,
			org.eclipse.jdt.compiler.apt.tests.AllTests.class,
			JavaElement8Tests.class,
		};
	}

	public static Class[] getConverterTestClasses() {
		return new Class[] {
				TypeAnnotationsConverterTest.class,
				ASTConverterTestAST8_2.class,
				ASTConverterAST8Test.class,
				ASTConverterBugsTestJLS8.class,
				ASTConverter15JLS8Test.class,
				ASTConverter18Test.class,
				ASTConverter17Test.class,
				ASTRewritingTest.class,
				TypeBindingTests308.class,
		};
	}

	public static Class[] getCompilerClasses() {
		return new Class[] {
			org.eclipse.jdt.core.tests.eval.TestAll.class,
			org.eclipse.jdt.core.tests.compiler.regression.TestAll.class,
		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunAllJava8Tests.class.getName());

		Class[] testClasses = getAllTestClasses();
		addTestsToSuite(ts, testClasses);
		testClasses = getConverterTestClasses();
		addTestsToSuite(ts, testClasses);

		AbstractCompilerTest.setpossibleComplianceLevels(AbstractCompilerTest.F_1_8);
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
