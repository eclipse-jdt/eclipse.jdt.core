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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.parser.LambdaExpressionSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.parser.ReferenceExpressionSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.parser.TypeAnnotationSyntaxTest;

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
		};
	}
	public static Test suite() {
		TestSuite ts = new TestSuite(RunAllJava8Tests.class.getName());

		Class[] testClasses = getAllTestClasses();
		for (int i = 0; i < testClasses.length; i++) {
			Class testClass = testClasses[i];
			// call the suite() method and add the resulting suite to the suite
			try {
				Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
				Test suite = (Test)suiteMethod.invoke(null, new Object[0]);
				ts.addTest(suite);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return ts;
	}
}
