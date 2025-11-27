/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contributions for
 *								bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 358903 - Filter practically unimportant resource leak warnings
 *								Bug 400874 - [1.8][compiler] Inference infrastructure should evolve to meet JLS8 18.x (Part G of JSR335 spec)
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *								Bug 383624 - [1.8][compiler] Revive code generation support for type annotations (from Olivier's work)
 *								bug 407191 - [1.8] Binary access support for type annotations
 *       Jesper Steen Moeller - Contributions for:
 *								Bug 406973 - [compiler] Parse MethodParameters attribute
 *								Bug 412153 - [1.8][compiler] Check validity of annotations which may be repeatable
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.compiler.util.HashtableOfObjectTest;
import org.eclipse.jdt.core.tests.compiler.util.JrtUtilTest;
import org.eclipse.jdt.core.tests.dom.StandAloneASTParserTest;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;

/**
 * Run all compiler regression tests
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestAll extends junit.framework.TestCase {

public TestAll(String testName) {
	super(testName);
}
public static Test suite() {

	// Common test suites
	ArrayList standardTests = new ArrayList();
	standardTests.add(ArrayTest.class);
	standardTests.add(AssignmentTest.class);
	standardTests.add(BooleanTest.class);
	standardTests.add(CastTest.class);
	standardTests.add(ClassFileComparatorTest.class);
	standardTests.add(CollisionCase.class);
	standardTests.add(ConstantTest.class);
	standardTests.add(DeprecatedTest.class);
	standardTests.add(LocalVariableTest.class);
	standardTests.add(LookupTest.class);
	standardTests.add(NumericTest.class);
	standardTests.add(ProblemConstructorTest.class);
	standardTests.add(ProblemTypeAndMethodTest.class);
	standardTests.add(ScannerTest.class);
	standardTests.add(PublicScannerTest.class);
	standardTests.add(SwitchTest.class);
	standardTests.add(TryStatementTest.class);
	standardTests.add(UtilTest.class);
	standardTests.add(XLargeTest.class);
	standardTests.add(InternalScannerTest.class);
	standardTests.add(ConditionalExpressionTest.class);
	standardTests.add(ExternalizeStringLiteralsTest.class);
	standardTests.add(NonFatalErrorTest.class);
	standardTests.add(FlowAnalysisTest.class);
	standardTests.add(CharOperationTest.class);
	standardTests.add(RuntimeTests.class);
	standardTests.add(DebugAttributeTest.class);
	standardTests.add(NullReferenceTest.class);
	standardTests.add(NullReferenceTestAsserts.class);
	if (UnconditionalFlowInfo.COVERAGE_TEST_FLAG) {
		standardTests.add(NullReferenceImplTests.class);
	}
	standardTests.add(CompilerInvocationTests.class);
	standardTests.add(InnerEmulationTest.class);
	standardTests.add(SuperTypeTest.class);
	standardTests.add(ForStatementTest.class);
	standardTests.add(FieldAccessTest.class);
	standardTests.add(SerialVersionUIDTests.class);
	standardTests.add(LineNumberAttributeTest.class);
	standardTests.add(ProgrammingProblemsTest.class);
	standardTests.add(ManifestAnalyzerTest.class);
	standardTests.add(InitializationTests.class);
	standardTests.add(ResourceLeakTests.class);
	standardTests.add(PackageBindingTest.class);
	standardTests.add(NameEnvironmentAnswerListenerTest.class);
	standardTests.add(XtextDependencies.class);

	// add all javadoc tests
	for (int i=0, l=JavadocTest.ALL_CLASSES.size(); i<l; i++) {
		standardTests.add(JavadocTest.ALL_CLASSES.get(i));
	}

	// Tests to run when compliance is greater than 1.3
	ArrayList since_1_4 = new ArrayList();
	since_1_4.add(AssertionTest.class);

	// Tests to run when compliance is greater than 1.4
	ArrayList since_1_5 = new ArrayList();
	since_1_5.addAll(RunComparableTests.ALL_CLASSES);
	since_1_5.add(ClassFileReaderTest_1_5.class);
	since_1_5.add(GenericTypeSignatureTest.class);
	since_1_5.add(InternalHexFloatTest.class);
	since_1_5.add(JavadocTest_1_5.class);
	since_1_5.add(BatchCompilerTest.class);
	since_1_5.add(NullAnnotationBatchCompilerTest.class);
	since_1_5.add(ConcurrentBatchCompilerTest.class);
	since_1_5.add(ExternalizeStringLiteralsTest_1_5.class);
	since_1_5.add(Deprecated15Test.class);
	since_1_5.add(InnerEmulationTest_1_5.class);
	since_1_5.add(AssignmentTest_1_5.class);
	since_1_5.add(InnerClass15Test.class);
	since_1_5.add(NullAnnotationTest.class);
	since_1_5.add(XLargeTest2.class);

	// Tests to run when compliance is greater than 1.5
	ArrayList since_1_6 = new ArrayList();
	since_1_6.add(StackMapAttributeTest.class);
	since_1_6.add(Compliance_1_6.class);

	ArrayList since_1_7 = new ArrayList();
	since_1_7.add(AssignmentTest_1_7.class);
	since_1_7.add(BinaryLiteralTest.class);
	since_1_7.add(UnderscoresInLiteralsTest.class);
	since_1_7.add(TryStatement17Test.class);
	since_1_7.add(TryWithResourcesStatementTest.class);
	since_1_7.add(GenericsRegressionTest_1_7.class);
	since_1_7.add(PolymorphicSignatureTest.class);
	since_1_7.add(Compliance_1_7.class);
	since_1_7.add(MethodHandleTest.class);
	since_1_7.add(ResourceLeakAnnotatedTests.class);


	ArrayList since_1_8 = new ArrayList();
	since_1_8.add(NegativeTypeAnnotationTest.class);
	since_1_8.add(NullTypeAnnotationTest.class);
	since_1_8.add(NegativeLambdaExpressionsTest.class);
	since_1_8.add(LambdaExpressionsTest.class);
	since_1_8.add(LambdaRegressionTest.class);
	since_1_8.add(SerializableLambdaTest.class);
	since_1_8.add(OverloadResolutionTest8.class);
	since_1_8.add(JSR335ClassFileTest.class);
	since_1_8.add(ExpressionContextTests.class);
	since_1_8.add(InterfaceMethodsTest.class);
	since_1_8.add(GrammarCoverageTests308.class);
	since_1_8.add(FlowAnalysisTest8.class);
	since_1_8.add(TypeAnnotationTest.class);
	since_1_8.add(JSR308SpecSnippetTests.class);
	since_1_8.add(Deprecated18Test.class);
	since_1_8.add(MethodParametersAttributeTest.class);
	since_1_8.add(ClassFileReaderTest_1_8.class);
	since_1_8.add(RepeatableAnnotationTest.class);
	since_1_8.add(GenericsRegressionTest_1_8.class);
	since_1_8.add(Unicode18Test.class);
	since_1_8.add(LambdaShapeTests.class);
	since_1_8.add(StringConcatTest.class);
	since_1_8.add(UseOfUnderscoreTest.class);
	since_1_8.add(DubiousOutcomeTest.class);

	ArrayList since_9 = new ArrayList();
	since_9.add(Unicode9Test.class);
	since_9.add(ModuleCompilationTests.class);
	since_9.add(GenericsRegressionTest_9.class);
	since_9.add(InterfaceMethodsTest_9.class);
	since_9.add(Deprecated9Test.class);
	since_9.add(ModuleAttributeTests.class);
	since_9.add(AutomaticModuleNamingTest.class);
	since_9.add(UnnamedModuleTest.class);
	since_9.add(NullAnnotationTests9.class);
	since_9.add(AnnotationTest_9.class);
	since_9.add(JavadocTestForModule.class);
	since_9.add(TryStatement9Test.class);

	// add 10 specific test here (check duplicates)
	ArrayList since_10 = new ArrayList();
	since_10.add(JEP286Test.class);
	since_10.add(Unicode10Test.class);

	// add 11 specific test here (check duplicates)
	ArrayList since_11 = new ArrayList();
	 since_11.add(JEP323VarLambdaParamsTest.class);
	 since_11.add(JEP181NestTest.class);
	 since_11.add(BatchCompilerTest2.class);

	// add 12 specific test here (check duplicates)
	 ArrayList since_12 = new ArrayList();
	 since_12.add(Unicode11Test.class);

		// add 13 specific test here (check duplicates)
	 ArrayList since_13 = new ArrayList();
	 since_13.add(Unicode12_1Test.class);

	 // add 14 specific test here (check duplicates)
	 ArrayList since_14 = new ArrayList();
	 since_14.add(SwitchExpressionsYieldTest.class);
	 since_14.add(BatchCompilerTest_14.class);

	 // add 15 specific test here (check duplicates)
	 ArrayList since_15 = new ArrayList();
	 since_15.add(ClassFileReaderTest_17.class);
	 since_15.add(JavadocTest_15.class);
	 since_15.add(Unicode13Test.class);
	 since_15.add(BatchCompilerTest_15.class);
	 since_15.add(TextBlockTest.class);
	 since_15.add(ExternalizeStringLiteralsTest_15.class);

	 // add 16 specific test here (check duplicates)
	 ArrayList since_16 = new ArrayList();
	 since_16.add(LocalEnumTest.class);
	 since_16.add(LocalStaticsTest.class);
	 since_16.add(PreviewFeatureTest.class);
	 since_16.add(ValueBasedAnnotationTests.class);
	 since_16.add(BatchCompilerTest_16.class);
	 since_16.add(PatternMatching16Test.class);
	 since_16.add(RecordsRestrictedClassTest.class);
	 since_16.add(JavadocTestForRecord.class);
	 since_16.add(JavadocTest_16.class);

	 // add 17 specific test here (check duplicates)
	 ArrayList since_17 = new ArrayList();
	 since_17.add(SealedTypesTests.class);
	 since_17.add(InstanceofPrimaryPatternTest.class);
	 since_17.add(BatchCompilerTest_17.class);

	 // add 18 specific test here (check duplicates)
	 ArrayList since_18 = new ArrayList();
	 since_18.add(JavadocTest_18.class);

	 // add 21 specific test here (check duplicates)
	 ArrayList since_21 = new ArrayList();
	 since_21.add(SwitchPatternTest.class);
	 since_21.add(RecordPatternTest.class);
	 since_21.add(RecordPatternProjectTest.class);
	 since_21.add(NullAnnotationTests21.class);
	 since_21.add(BatchCompilerTest_21.class);
	 since_21.add(JEP441SnippetsTest.class);


	 // add 21 specific test here (check duplicates)
	 ArrayList since_22 = new ArrayList();
	 since_22.add(UnnamedPatternsAndVariablesTest.class);
	 since_22.add(UseOfUnderscoreJava22Test.class);
	 since_22.add(SwitchPatternTest22.class);

	 ArrayList since_23 = new ArrayList();
	 since_23.add(MarkdownCommentsTest.class);

	 ArrayList since_25 = new ArrayList();
	 since_25.add(ModuleImportTests.class);
	 since_25.add(SuperAfterStatementsTest.class);
	 since_25.add(ImplicitlyDeclaredClassesTest.class);

	 ArrayList since_26 = new ArrayList();
	 since_26.add(PreviewFlagTest.class);
	 since_26.add(PrimitiveInPatternsTest.class);
	 since_26.add(PrimitiveInPatternsTestSH.class);

	record TestsAddition(ArrayList newTests, long testLevel, long jdkVersion) {}

	List<TestsAddition> testAdditionsList = new ArrayList<>();
	Collections.addAll(testAdditionsList, new TestsAddition[] {
			 new TestsAddition(since_10, AbstractCompilerTest.F_10, ClassFileConstants.JDK10),
			 new TestsAddition(since_11, AbstractCompilerTest.F_11, ClassFileConstants.JDK11),
			 new TestsAddition(since_12, AbstractCompilerTest.F_12, ClassFileConstants.JDK12),
			 new TestsAddition(since_13, AbstractCompilerTest.F_13, ClassFileConstants.JDK13),
			 new TestsAddition(since_14, AbstractCompilerTest.F_14, ClassFileConstants.JDK14),
			 new TestsAddition(since_15, AbstractCompilerTest.F_15, ClassFileConstants.JDK15),
			 new TestsAddition(since_16, AbstractCompilerTest.F_16, ClassFileConstants.JDK16),
			 new TestsAddition(since_17, AbstractCompilerTest.F_17, ClassFileConstants.JDK17),
			 new TestsAddition(since_18, AbstractCompilerTest.F_18, ClassFileConstants.JDK18),
			 new TestsAddition(new ArrayList(), AbstractCompilerTest.F_19, ClassFileConstants.JDK19),
			 new TestsAddition(new ArrayList(), AbstractCompilerTest.F_20, ClassFileConstants.JDK20),
			 new TestsAddition(since_21, AbstractCompilerTest.F_21, ClassFileConstants.JDK21),
			 new TestsAddition(since_22, AbstractCompilerTest.F_22, ClassFileConstants.JDK22),
			 new TestsAddition(since_23, AbstractCompilerTest.F_23, ClassFileConstants.JDK23),
			 new TestsAddition(new ArrayList(), AbstractCompilerTest.F_24, ClassFileConstants.JDK24),
			 new TestsAddition(since_25, AbstractCompilerTest.F_25, ClassFileConstants.JDK25),
			 new TestsAddition(since_26, AbstractCompilerTest.F_26, ClassFileConstants.JDK26),
	});
	// Build final test suite
	TestSuite all = new TestSuite(TestAll.class.getName());
	all.addTest(new TestSuite(StandAloneASTParserTest.class));
	all.addTest(new TestSuite(HashtableOfObjectTest.class));
	all.addTest(new TestSuite(JrtUtilTest.class));
	int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();

	// The tests for older compliances are only run at F_1_8
	// So, first run them before the cumulative/iterative addition takes over
	if ((possibleComplianceLevels & AbstractCompilerTest.F_1_8) != 0) {
		ArrayList tests_1_8 = (ArrayList)standardTests.clone();
		tests_1_8.add(Compliance_1_3.class);
		tests_1_8.add(JavadocTest_1_3.class);
		tests_1_8.add(Compliance_CLDC.class);
		tests_1_8.add(Compliance_1_4.class);
		tests_1_8.add(ClassFileReaderTest_1_4.class);
		tests_1_8.add(JavadocTest_1_4.class);
		tests_1_8.addAll(since_1_4);
		tests_1_8.addAll(since_1_5);
		tests_1_8.addAll(since_1_6);
		tests_1_8.addAll(since_1_7);
		tests_1_8.addAll(since_1_8);
		TestCase.resetForgottenFilters(tests_1_8);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK1_8, tests_1_8));
	}
	ArrayList cumulative_tests = (ArrayList)standardTests.clone();
	if ((possibleComplianceLevels & AbstractCompilerTest.F_9) != 0) {
		cumulative_tests.addAll(since_1_4);
		cumulative_tests.addAll(since_1_5);
		cumulative_tests.addAll(since_1_6);
		cumulative_tests.addAll(since_1_7);
		cumulative_tests.addAll(since_1_8);
		cumulative_tests.addAll(since_9);
		TestCase.resetForgottenFilters(cumulative_tests);
		all.addTest(AbstractCompilerTest.buildComplianceTestSuite(ClassFileConstants.JDK9, cumulative_tests));
	}

	for (TestsAddition testList : testAdditionsList) {
		if ((possibleComplianceLevels & testList.testLevel) != 0) {
			cumulative_tests.addAll(testList.newTests);
			TestCase.resetForgottenFilters(cumulative_tests);
			all.addTest(AbstractCompilerTest.buildComplianceTestSuite(testList.jdkVersion, cumulative_tests));
		}
	}

	all.addTest(new TestSuite(Jsr14Test.class));
	return all;
}
}
