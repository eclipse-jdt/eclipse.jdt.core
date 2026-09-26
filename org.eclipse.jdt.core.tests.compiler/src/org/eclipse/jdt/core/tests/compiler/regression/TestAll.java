/*******************************************************************************
 * Copyright (c) 2026 GK Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.core.tests.compiler.unicode.*;
import org.eclipse.jdt.core.tests.compiler.util.HashtableOfObjectTest;
import org.eclipse.jdt.core.tests.compiler.util.JrtUtilTest;
import org.eclipse.jdt.core.tests.dom.StandAloneASTParserTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    ArrayTest.class,
	AssignmentTest.class,
	BooleanTest.class,
	CastTest.class,
	ClassFileComparatorTest.class,
	CollisionCase.class,
	ConstantTest.class,
	DeprecatedTest.class,
	LocalVariableTest.class,
	LookupTest.class,
	NumericTest.class,
	ProblemConstructorTest.class,
	ProblemTypeAndMethodTest.class,
	ScannerTest.class,
	PublicScannerTest.class,
	SwitchTest.class,
	TryStatementTest.class,
	UtilTest.class,
	XLargeTest.class,
	InternalScannerTest.class,
	ConditionalExpressionTest.class,
	ExternalizeStringLiteralsTest.class,
	NonFatalErrorTest.class,
	FlowAnalysisTest.class,
	CharOperationTest.class,
	RuntimeTests.class,
	DebugAttributeTest.class,
	NullReferenceTest.class,
	NullReferenceTestAsserts.class,
	CompilerInvocationTests.class,
	InnerEmulationTest.class,
	SuperTypeTest.class,
	ForStatementTest.class,
	FieldAccessTest.class,
	SerialVersionUIDTests.class,
	LineNumberAttributeTest.class,
	ProgrammingProblemsTest.class,
	ManifestAnalyzerTest.class,
	InitializationTests.class,
	ResourceLeakTests.class,
	PackageBindingTest.class,
	NameEnvironmentAnswerListenerTest.class,
	XtextDependencies.class,
	// 1.5 - 1.7 (always enabled):
	AssertionTest.class,
	RunComparableTests.class, // original set of run.javac tests
	ClassFileReaderTest_1_5.class,
	GenericTypeSignatureTest.class,
	InternalHexFloatTest.class,
	BatchCompilerTest.class,
	NullAnnotationBatchCompilerTest.class,
	ConcurrentBatchCompilerTest.class,
	ExternalizeStringLiteralsTest_1_5.class,
	Deprecated15Test.class,
	InnerEmulationTest_1_5.class,
	AssignmentTest_1_5.class,
	InnerClass15Test.class,
	NullAnnotationTest.class,
	XLargeTest2.class,
	StackMapAttributeTest.class,
	Compliance_1_6.class,
	AssignmentTest_1_7.class,
	BinaryLiteralTest.class,
	UnderscoresInLiteralsTest.class,
	TryStatement17Test.class,
	TryWithResourcesStatementTest.class,
	GenericsRegressionTest_1_7.class,
	PolymorphicSignatureTest.class,
	Compliance_1_7.class,
	MethodHandleTest.class,
	ResourceLeakAnnotatedTests.class,
	// 1.8 (always enabled):
	NegativeTypeAnnotationTest.class,
	NullTypeAnnotationTest.class,
	NegativeLambdaExpressionsTest.class,
	LambdaExpressionsTest.class,
	NestedLambdaInferenceTest.class,
	LambdaRegressionTest.class,
	SerializableLambdaTest.class,
	OverloadResolutionTest8.class,
	JSR335ClassFileTest.class,
	ExpressionContextTests.class,
	InterfaceMethodsTest.class,
	GrammarCoverageTests308.class,
	FlowAnalysisTest8.class,
	TypeAnnotationTest.class,
	JSR308SpecSnippetTests.class,
	Deprecated18Test.class,
	MethodParametersAttributeTest.class,
	ClassFileReaderTest_1_8.class,
	RepeatableAnnotationTest.class,
	GenericsRegressionTest_1_8.class,
	Unicode1_8Test.class,
	LambdaShapeTests.class,
	StringConcatTest.class,
	UseOfUnderscoreTest.class,
	DubiousOutcomeTest.class,

    // javadoc suite:
    AllJavadocTests.class,

	// specific to ancient versions that are no longer supported (run only at 1.8):
	Compliance_1_3.class,
	Compliance_CLDC.class,
	Compliance_1_4.class,
	ClassFileReaderTest_1_4.class,

	// since 9
	Unicode9Test.class,
	ModuleCompilationTests.class,
	GenericsRegressionTest_9.class,
	InterfaceMethodsTest_9.class,
	Deprecated9Test.class,
	ModuleAttributeTests.class,
	AutomaticModuleNamingTest.class,
	UnnamedModuleTest.class,
	NullAnnotationTests9.class,
	AnnotationTest_9.class,
	TryStatement9Test.class,
	// since 10
	JEP286Test.class,
	Unicode10Test.class,
	// since 11
	JEP323VarLambdaParamsTest.class,
	JEP181NestTest.class,
	BatchCompilerTest2.class,
	// since 12
	Unicode11Test.class,
	// since 13
	Unicode12_1Test.class,
	// since 14
	SwitchExpressionsYieldTest.class,
	BatchCompilerTest_14.class,
	// since 15
	ClassFileReaderTest_17.class,
	Unicode13Test.class,
	BatchCompilerTest_15.class,
	TextBlockTest.class,
	ExternalizeStringLiteralsTest_15.class,
	// since 16
	LocalEnumTest.class,
	LocalStaticsTest.class,
	PreviewFeatureTest.class,
	ValueBasedAnnotationTests.class,
	BatchCompilerTest_16.class,
	PatternMatching16Test.class,
	RecordsRestrictedClassTest.class,
	// since 17
	SealedTypesTests.class,
	InstanceofPrimaryPatternTest.class,
	BatchCompilerTest_17.class,
	// since 18 see AllJavadocTests
	// since 19
	Unicode14Test.class,
	// since 20
	Unicode15Test.class,
	// since 21
	SwitchPatternTest.class,
	RecordPatternTest.class,
	RecordPatternProjectTest.class,
	NullAnnotationTests21.class,
	BatchCompilerTest_21.class,
	JEP441SnippetsTest.class,
	// since 22
	UnnamedPatternsAndVariablesTest.class,
	UseOfUnderscoreJava22Test.class,
	SwitchPatternTest22.class,
	Unicode15_1Test.class,
	// since 23
	MarkdownCommentsTest.class,
	// since 24
	Unicode16Test.class,
	// since 25
	ModuleImportTests.class,
	SuperAfterStatementsTest.class,
	ImplicitlyDeclaredClassesTest.class,
	// since 26
	Unicode17Test.class,
	// since 26
	PreviewFlagTest.class,
	PrimitiveInPatternsTest.class,
	PrimitiveInPatternsTestSH.class,

	// single run (some still at JUnit3):
	StandAloneASTParserTest.class,
	HashtableOfObjectTest.class,
	JrtUtilTest.class,
	org.eclipse.jdt.core.tests.compiler.util.UtilTest.class,
	Jsr14Test.class,
	PrintRunJavacStats.class // the very last test to capture statistics of all tests
})
public class TestAll { }
