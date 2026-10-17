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
package org.eclipse.jdt.core.tests.dom;

import org.eclipse.test.TracingSuite;
import org.eclipse.test.TracingSuite.TracingOptions;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(TracingSuite.class)
@SuiteClasses(RunAllTests.class)
// These headless tests have no UI to capture. Keep timeout thread dumps without requiring X11.
@TracingOptions(stackDumpTimeoutSeconds = 60, maxScreenshotCount = 0)
public class RunAllTestsTracing {
}
