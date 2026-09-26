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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Run all compiler regression tests
 */
@Suite
@SelectClasses({
	AmbiguousMethodTest.class,
	AutoBoxingTest.class,
	SuppressWarningsTest.class,
	Compliance_1_5.class,
	GenericTypeTest.class,
	GenericsRegressionTest.class,
	ForeachStatementTest.class,
	StaticImportTest.class,
	VarargsTest.class,
	EnumTest.class,
	MethodVerifyTest.class,
	AnnotationTest.class,
	EnclosingMethodAttributeTest.class,
})
public class RunComparableTests { }
