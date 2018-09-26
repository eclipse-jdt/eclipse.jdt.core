/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.performance.util;

import org.eclipse.test.internal.performance.PerformanceMeterFactory;
import org.eclipse.test.performance.PerformanceMeter;


public class JdtCorePerformanceMeterFactory extends PerformanceMeterFactory {
	protected PerformanceMeter doCreatePerformanceMeter(String scenario) {
		return new JdtCorePerformanceMeter(scenario);
	}
}
