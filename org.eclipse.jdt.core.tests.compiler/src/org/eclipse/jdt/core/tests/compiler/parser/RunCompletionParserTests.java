/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
	AllocationExpressionCompletionTest.class,
	ClassLiteralAccessCompletionTest.class,
	CompletionParserTest.class,
	CompletionParserTest2.class,
	CompletionParserTestKeyword.class,
	CompletionRecoveryTest.class,
	DietCompletionTest.class,
	ExplicitConstructorInvocationCompletionTest.class,
	FieldAccessCompletionTest.class,
	InnerTypeCompletionTest.class,
	JavadocCompletionParserTest.class,
	LabelStatementCompletionTest.class,
	MethodInvocationCompletionTest.class,
	NameReferenceCompletionTest.class,
	ReferenceTypeCompletionTest.class,

	GenericsCompletionParserTest.class,
	EnumCompletionParserTest.class,
	AnnotationCompletionParserTest.class,

	MarkdownCompletionParserTest.class,
})
public class RunCompletionParserTests { }
