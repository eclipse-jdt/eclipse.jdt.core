/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
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
import org.eclipse.jdt.core.tests.builder.IncrementalTests;
import org.eclipse.jdt.core.tests.compiler.regression.*;
import org.eclipse.jdt.core.tests.dom.ASTConverter_15Test;
import org.eclipse.jdt.core.tests.dom.ASTConverter_17Test;
import org.eclipse.jdt.core.tests.dom.ASTTest;
import org.eclipse.jdt.core.tests.dom.ConverterTestSetup;
import org.eclipse.jdt.core.tests.formatter.FormatterBugsTests;
import org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests;
import org.eclipse.jdt.core.tests.model.*;
import org.eclipse.jdt.core.tests.rewrite.describing.ASTRewritingTypeDeclTest;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunVariousSealedTypeTests extends TestCase {

	public RunVariousSealedTypeTests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
//				SealedTypeElementProcessor.class,
				IncrementalTests.class,
				BatchCompilerTest_15.class,
				ClassFileReaderTest_17.class,
				NegativeTypeAnnotationTest.class,
				NullAnnotationTests21.class,
				PatternMatching16Test.class,
				RecordPatternProjectTest.class,
				RecordPatternTest.class,
				RecordsRestrictedClassTest.class,
				ScannerTest.class,
				SealedTypesTests.class,
				JEP441SnippetsTest.class,
				SwitchPatternTest.class,
				SwitchPatternTest22.class,
				UnnamedPatternsAndVariablesTest.class,
				ASTRewritingTypeDeclTest.class,
				ASTConverter_15Test.class,
				ASTConverter_17Test.class,
				ASTTest.class,
				FormatterBugsTests.class,
				FormatterRegressionTests.class,
				CompletionTests16_1.class,
				CompletionTests17.class,
				Java21ElementTests.class,
				JavaSearchBugs15Tests.class,
				JavaSearchBugs17Tests.class,
				JavaSearchBugs19Tests.class,
				ReconcilerTests.class,
				ReconcilerTests21.class,
				ResolveTests21.class,
				SealedTypeModelTests.class,
				TypeHierarchyTests.class,
		};
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(RunVariousSealedTypeTests.class.getName());

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