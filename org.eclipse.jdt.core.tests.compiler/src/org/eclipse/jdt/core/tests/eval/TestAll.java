/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.eval;

import junit.framework.Test;
import junit.framework.TestSuite;
/**
 * Run all tests defined in this package.
 */
public class TestAll extends EvaluationTest {
public TestAll(String name) {
	super(name);
}
public static Test suite() {
	return suite(JRE_PATH, EVAL_DIRECTORY);
}
public static Test suite(String jrePath, String evalDirectory) {
	TestSuite suite = new TestSuite(TestAll.class.getName());
	suite.addTest(suite(SanityTestEvaluationContext.class));
	suite.addTest(suite(SanityTestEvaluationResult.class));
	suite.addTest(suite(VariableTest.class));
	suite.addTest(suite(CodeSnippetTest.class));
	suite.addTest(suite(NegativeCodeSnippetTest.class));
	suite.addTest(suite(NegativeVariableTest.class));
	suite.addTest(suite(DebugEvaluationTest.class));
	EvaluationSetup evalSetup = new DebugEvaluationSetup(suite);
	evalSetup.jrePath = jrePath;
	evalSetup.evalDirectory = evalDirectory;
	return evalSetup;
}
}
