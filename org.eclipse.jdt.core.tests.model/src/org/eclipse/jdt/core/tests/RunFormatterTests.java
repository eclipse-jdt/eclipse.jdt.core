/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests;
import org.eclipse.jdt.core.tests.formatter.comment.CommentsTestSuite;

/**
 * Runs all formatter tests.
 */
public class RunFormatterTests extends TestCase {
	
	public static Class[] getAllTestClasses() {
		return new Class[] {
			FormatterRegressionTests.class,
			CommentsTestSuite.class,
		};
	}
	public static Test suite() {
		TestSuite ts = new TestSuite(RunFormatterTests.class.getName());

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
	
public RunFormatterTests(String name) {
	super(name);
}
}

