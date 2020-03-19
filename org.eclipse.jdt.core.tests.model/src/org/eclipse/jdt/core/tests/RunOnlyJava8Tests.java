/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
 *     Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                       Bug 405104 - [1.8][compiler][codegen] Implement support for serializeable lambdas
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.builder.IncrementalTests18;
import org.eclipse.jdt.core.tests.compiler.parser.CompletionParserTest18;
import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.parser.LambdaExpressionSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.parser.ReferenceExpressionSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.parser.SelectionParserTest18;
import org.eclipse.jdt.core.tests.compiler.parser.TypeAnnotationSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.regression.ClassFileReaderTest_1_8;
import org.eclipse.jdt.core.tests.compiler.regression.ConditionalExpressionTest;
import org.eclipse.jdt.core.tests.compiler.regression.Deprecated18Test;
import org.eclipse.jdt.core.tests.compiler.regression.ExpressionContextTests;
import org.eclipse.jdt.core.tests.compiler.regression.CompilerInvocationTests;
import org.eclipse.jdt.core.tests.compiler.regression.FlowAnalysisTest8;
import org.eclipse.jdt.core.tests.compiler.regression.GenericsRegressionTest_1_8;
import org.eclipse.jdt.core.tests.compiler.regression.GrammarCoverageTests308;
import org.eclipse.jdt.core.tests.compiler.regression.InterfaceMethodsTest;
import org.eclipse.jdt.core.tests.compiler.regression.JSR308SpecSnippetTests;
import org.eclipse.jdt.core.tests.compiler.regression.JSR335ClassFileTest;
import org.eclipse.jdt.core.tests.compiler.regression.LambdaExpressionsTest;
import org.eclipse.jdt.core.tests.compiler.regression.LambdaRegressionTest;
import org.eclipse.jdt.core.tests.compiler.regression.LambdaShapeTests;
import org.eclipse.jdt.core.tests.compiler.regression.MethodParametersAttributeTest;
import org.eclipse.jdt.core.tests.compiler.regression.NegativeLambdaExpressionsTest;
import org.eclipse.jdt.core.tests.compiler.regression.NegativeTypeAnnotationTest;
import org.eclipse.jdt.core.tests.compiler.regression.NullTypeAnnotationTest;
import org.eclipse.jdt.core.tests.compiler.regression.OverloadResolutionTest8;
import org.eclipse.jdt.core.tests.compiler.regression.RepeatableAnnotationTest;
import org.eclipse.jdt.core.tests.compiler.regression.SerializableLambdaTest;
import org.eclipse.jdt.core.tests.compiler.regression.TypeAnnotationTest;
import org.eclipse.jdt.core.tests.compiler.regression.Unicode18Test;
import org.eclipse.jdt.core.tests.dom.ASTConverter15JLS8Test;
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

@SuppressWarnings({"rawtypes", "unchecked"})
public class RunOnlyJava8Tests extends TestCase {

	public RunOnlyJava8Tests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			LambdaExpressionSyntaxTest.class,
			NegativeLambdaExpressionsTest.class,
			LambdaExpressionsTest.class,
			LambdaRegressionTest.class,
			LambdaShapeTests.class,
			SerializableLambdaTest.class,
			OverloadResolutionTest8.class,
			JSR335ClassFileTest.class,
			NegativeTypeAnnotationTest.class,
			TypeAnnotationSyntaxTest.class,
			ReferenceExpressionSyntaxTest.class,
			InterfaceMethodsTest.class,
			ComplianceDiagnoseTest.class,
			GrammarCoverageTests308.class,
			NullTypeAnnotationTest.class,
			CompilerInvocationTests.class,
			ExpressionContextTests.class,
			FlowAnalysisTest8.class,
			FormatterJSR335Tests.class,
			FormatterJSR308Tests.class,
			FormatterBugs18Tests.class,
			JavaSearchBugs8Tests.class,
			TypeAnnotationTest.class,
			JSR308SpecSnippetTests.class,
			Deprecated18Test.class,
			MethodParametersAttributeTest.class,
			ClassFileReaderTest_1_8.class,
			RepeatableAnnotationTest.class,
			ResolveTests18.class,
			CompletionParserTest18.class,
			SelectionParserTest18.class,
			CompletionTests18.class,
			GenericsRegressionTest_1_8.class,
			IncrementalTests18.class,
			ConditionalExpressionTest.class,
			Unicode18Test.class,
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
				ASTRewritingTest.class,
				TypeBindingTests308.class,
		};
	}
	public static Test suite() {
		TestSuite ts = new TestSuite(RunOnlyJava8Tests.class.getName());

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
