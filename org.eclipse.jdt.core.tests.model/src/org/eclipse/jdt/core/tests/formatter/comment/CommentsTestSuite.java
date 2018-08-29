/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.formatter.comment;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @since 3.0
 */
public class CommentsTestSuite extends TestSuite {

	public static Test suite() {
		return new CommentsTestSuite();
	}

	public CommentsTestSuite() {
		addTest(MultiLineTestCase.suite());
		addTest(SingleLineTestCase.suite());
		addTest(JavaDocTestCase.suite());
	}
}
