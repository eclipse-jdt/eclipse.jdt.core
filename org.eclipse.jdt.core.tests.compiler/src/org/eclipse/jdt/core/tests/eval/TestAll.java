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
package org.eclipse.jdt.core.tests.eval;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
/**
 * Run all tests defined in this package.
 */
@Suite
@SelectClasses({
	SanityTestEvaluationContext.class,
	SanityTestEvaluationResult.class,
	VariableTest.class,
	CodeSnippetTest.class,
	NegativeCodeSnippetTest.class,
	NegativeVariableTest.class,
	DebugEvaluationTest.class,
	EvaluationContextWrapperTest.class
})
public class TestAll  { }
