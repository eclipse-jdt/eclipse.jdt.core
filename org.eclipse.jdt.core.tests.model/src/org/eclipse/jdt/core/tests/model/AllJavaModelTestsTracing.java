/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import org.eclipse.test.TracingSuite;
import org.eclipse.test.TracingSuite.TracingOptions;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(TracingSuite.class)
@SuiteClasses(AllJavaModelTests.class)
@TracingOptions(stackDumpTimeoutSeconds = 60)
public class AllJavaModelTestsTracing {
}
