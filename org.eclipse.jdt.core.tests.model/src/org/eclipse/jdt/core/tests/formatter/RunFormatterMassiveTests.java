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
package org.eclipse.jdt.core.tests.formatter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;

/**
 * Runs all formatter tests.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class RunFormatterMassiveTests extends junit.framework.TestCase {

	private final static File INPUT_DIR = new File(System.getProperty("inputDir"));
	private final static String PROFILE = System.getProperty("profiles");
	private final static String[] SUPPORTED_WORKSPACES = {
		"full-src-30",
		"galileo",
		"JDKs",
	};
	private final static String[] SUPPORTED_PROFILES = {
		"",
		"no_comments=true",
		"join_lines=never",
		"join_lines=only_comments,braces=next_line",
	};

public static Test suite() {
	TestSuite ts = new TestSuite(RunFormatterMassiveTests.class.getName());

	// Reset forgotten subsets of tests
	TestCase.TESTS_PREFIX = null;
	TestCase.TESTS_NAMES = null;
	TestCase.TESTS_NUMBERS = null;
	TestCase.TESTS_RANGE = null;
	TestCase.RUN_ONLY_ID = null;

	// Add all tests suite of tests
	String wksp = System.getProperty("wksp");
	boolean valid = false;
	Map workspaces = new HashMap();
	for (int i=0; i<SUPPORTED_WORKSPACES.length; i++) {
		if (wksp == null || wksp.equals(SUPPORTED_WORKSPACES[i])) {
			File inputDir = new File(INPUT_DIR, SUPPORTED_WORKSPACES[i]);
			if (PROFILE == null) {
				for (int j=0; j< SUPPORTED_PROFILES.length; j++) {
					addClass(ts, inputDir, SUPPORTED_PROFILES[j], workspaces);
				}
			} else {
				addClass(ts, inputDir, PROFILE, workspaces);
			}
			valid = true;
		}
	}
	if (!valid) {
		System.err.println(wksp+" is not a valid workspace name!!!");
	}
	return ts;
}

private static void addClass(TestSuite ts, File inputDir, String profile, Map workspaces) {
	Class testClass = FormatterMassiveRegressionTests.class;

	// call the suite() method and add the resulting suite to the suite
	try {
		Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[] { File.class, String.class, Map.class }); //$NON-NLS-1$
		Test suite = (Test)suiteMethod.invoke(null, new Object[] { inputDir, profile, workspaces });
		ts.addTest(suite);
	} catch (IllegalAccessException e) {
		e.printStackTrace();
	} catch (InvocationTargetException e) {
		e.getTargetException().printStackTrace();
	} catch (NoSuchMethodException e) {
		e.printStackTrace();
	}

}
public RunFormatterMassiveTests(String name) {
	super(name);
}
}

