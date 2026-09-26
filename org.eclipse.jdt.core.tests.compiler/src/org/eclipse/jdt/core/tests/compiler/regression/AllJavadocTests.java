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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
	JavadocBugsTest.class,
	JavadocTestForMethod.class,
	JavadocTestMixed.class,
	JavadocTestForClass.class,
	JavadocTestForConstructor.class,
	JavadocTestForField.class,
	JavadocTestForInterface.class,
	JavadocTestOptions.class,

	//	legacy:
	JavadocTest_1_3.class,
	JavadocTest_1_4.class,
	JavadocTest_1_5.class,
	// since 9
	JavadocTestForModule.class,
	// since 15
	JavadocTest_15.class,
	// since 16
	JavadocTestForRecord.class,
	JavadocTest_16.class,
	// since 18
	JavadocTest_18.class,
})
public class AllJavadocTests { }
