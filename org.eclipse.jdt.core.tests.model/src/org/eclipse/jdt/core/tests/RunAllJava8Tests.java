/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.parser.LambdaExpressionSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.parser.ReferenceExpressionSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.parser.TypeAnnotationSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.regression.DefaultMethodsTest;
import org.eclipse.jdt.core.tests.compiler.regression.GrammarCoverageTests308;
import org.eclipse.jdt.core.tests.compiler.regression.NegativeLambdaExpressionsTest;
import org.eclipse.jdt.core.tests.compiler.regression.NegativeTypeAnnotationTest;
import org.eclipse.jdt.core.tests.compiler.regression.NullTypeAnnotationTest;
import org.eclipse.jdt.core.tests.dom.ASTConverter15JLS8Test;
import org.eclipse.jdt.core.tests.dom.ASTConverterAST8Test;
import org.eclipse.jdt.core.tests.dom.ASTConverterBugsTestJLS8;
import org.eclipse.jdt.core.tests.dom.ASTConverterTestAST8_2;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.dom.TypeAnnotationsConverterTest;

public class RunAllJava8Tests extends TestCase {
	
	public RunAllJava8Tests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			LambdaExpressionSyntaxTest.class,
			NegativeLambdaExpressionsTest.class,
			NegativeTypeAnnotationTest.class,
			TypeAnnotationSyntaxTest.class,
			ReferenceExpressionSyntaxTest.class,
			DefaultMethodsTest.class,
			ComplianceDiagnoseTest.class,
			GrammarCoverageTests308.class,
			NullTypeAnnotationTest.class
		};
	}
	
	public static Class[] getConverterTestClasses() {
		return new Class[] {
				TypeAnnotationsConverterTest.class,
				ASTConverterTestAST8_2.class,
				ASTConverterAST8Test.class,
				ASTConverterBugsTestJLS8.class,
				ASTConverter15JLS8Test.class,
		};
	}
	public static Test suite() {
		TestSuite ts = new TestSuite(RunAllJava8Tests.class.getName());

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
