/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
 *      Jesper Steen Møller <jesper@selskabet.org> - Contributions for
 *			bug 527554 - [18.3] Compiler support for JEP 286 Local-Variable Type
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.core.tests.compiler.regression.JEP286ReservedWordTest;
import org.eclipse.jdt.core.tests.junit5.extension.TestCase;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
	/* completion */
	AllocationExpressionCompletionTest.class,
	AnnotationCompletionParserTest.class,
	ClassLiteralAccessCompletionTest.class,
	CompletionParserTest.class,
	CompletionParserTest18.class,
	CompletionParserTest2.class,
	CompletionParserTestKeyword.class,
	CompletionRecoveryTest.class,
	DietCompletionTest.class,
	EnumCompletionParserTest.class,
	ExplicitConstructorInvocationCompletionTest.class,
	FieldAccessCompletionTest.class,
	GenericsCompletionParserTest.class,
	InnerTypeCompletionTest.class,
	JavadocCompletionParserTest.class,
	LabelStatementCompletionTest.class,
	MethodInvocationCompletionTest.class,
	NameReferenceCompletionTest.class,
	ReferenceTypeCompletionTest.class,
	StringLiteralTest.class,

	/* selection */
	AnnotationSelectionTest.class,
	EnumSelectionTest.class,
	ExplicitConstructorInvocationSelectionTest.class,
	GenericsSelectionTest.class,
	SelectionTest.class,
	SelectionTest2.class,
	SelectionJavadocTest.class,

	ParserTest.class,
	ParserTest1_7.class,

	/* recovery tests */
	AnnotationDietRecoveryTest.class,
	DietRecoveryTest.class,
	EnumDietRecoveryTest.class,
	GenericDietRecoveryTest.class,
	StatementRecoveryTest_1_5.class,
	StatementRecoveryTest.class,

	/* source element parser tests */
	SourceElementParserTest.class,

	/* document element parser tests */
	DocumentElementParserTest.class,

	/* syntax error diagnosis tests */
	ComplianceDiagnoseTest.class,
	DualParseSyntaxErrorTest.class,
	SyntaxErrorTest.class,

	/* 8 */
	LambdaExpressionSyntaxTest.class,
	ReferenceExpressionSyntaxTest.class,
	TypeAnnotationSyntaxTest.class,
	SelectionParserTest18.class,

	/* 9 */
	SelectionParserTest9.class,
	ModuleDeclarationSyntaxTest.class,

	/* 10 */
	SelectionParserTest10.class,
	JEP286ReservedWordTest.class,

	/* 14 */
	SelectionParserTest14.class,

	/* 16 */
	PatternMatchingSelectionTest.class,

	/* 23 */
	MarkdownCompletionParserTest.class,
	SelectionMarkdownTest.class,
})
public class TestAll {
	@BeforeSuite
	public static void clearFilters() {
		// disable forgotten subsets tests
		TestCase.DISABLE_FILTERS = true;
	}
}
