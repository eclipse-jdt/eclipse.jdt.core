/*******************************************************************************
* Copyright (c) 2025 Advantest Europe GmbH and others.
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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.compiler.apt.tests.RecordElementsTests;
import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.regression.NullAnnotationTests21;
import org.eclipse.jdt.core.tests.compiler.regression.RecordPatternProjectTest;
import org.eclipse.jdt.core.tests.compiler.regression.RecordPatternTest;
import org.eclipse.jdt.core.tests.compiler.regression.RecordsRestrictedClassTest;
import org.eclipse.jdt.core.tests.compiler.regression.SealedTypesTests;
import org.eclipse.jdt.core.tests.compiler.regression.SwitchPatternTest;
import org.eclipse.jdt.core.tests.compiler.regression.SwitchPatternTest22;
import org.eclipse.jdt.core.tests.dom.ASTConverter_RecordPattern_Test;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.model.CompletionTestsForRecordPattern;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingRecordDeclarationTest;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingRecordPatternTest;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingSwitchPatternTest;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunVariousRecordsTest extends TestCase {

	public RunVariousRecordsTest(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
				RecordsRestrictedClassTest.class,
				SealedTypesTests.class,
				RecordPatternTest.class,
				RecordPatternProjectTest.class,
				SwitchPatternTest.class,
				ASTConverter_RecordPattern_Test.class,
				ASTRewritingRecordPatternTest.class,
				ASTRewritingSwitchPatternTest.class,
				ASTRewritingRecordDeclarationTest.class,
				SwitchPatternTest22.class,
				CompletionTestsForRecordPattern.class,
				NullAnnotationTests21.class,
				ComplianceDiagnoseTest.class,
				RecordElementsTests.class,
		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunVariousRecordsTest.class.getName());

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
