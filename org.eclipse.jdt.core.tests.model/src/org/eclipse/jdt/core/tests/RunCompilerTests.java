/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

/**
 * Runs all compiler tests (including parser tests and evaluation tests) in all compliance mode.
 * Use -Dcompliance=1.4 as a VM argument if you want to run in 1.4 compliance mode only.
 * See AbstractCompilerTests for more details.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RunCompilerTests extends TestCase {

public RunCompilerTests(String name) {
	super(name);
}
public static Class[] getAllTestClasses() {
	return new Class[] {
		org.eclipse.jdt.core.tests.compiler.regression.TestAll.class,
		org.eclipse.jdt.core.tests.compiler.parser.TestAll.class,
		org.eclipse.jdt.core.tests.eval.TestAll.class,
	};
}
public static Test suite() {
	TestSuite ts = new TestSuite(RunCompilerTests.class.getName());

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

