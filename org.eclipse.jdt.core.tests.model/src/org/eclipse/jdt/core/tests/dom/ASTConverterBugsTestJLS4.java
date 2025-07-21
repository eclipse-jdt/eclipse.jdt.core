/*******************************************************************************
 * Copyright (c) 2011, 2015 IBM Corporation and others.
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

import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite to verify that DOM/AST bugs are fixed.
 *
 * Note that only specific JLS4 tests are defined in this test suite, but when
 * running it, all superclass {@link ASTConverterBugsTestJLS3} tests will be run
 * as well.
 */
@SuppressWarnings("rawtypes")
public class ASTConverterBugsTestJLS4 extends ASTConverterBugsTestJLS3 {

public ASTConverterBugsTestJLS4(String name) {
    super(name);
    this.testLevel = getJLS4();
}

public static Test suite() {
	TestSuite suite = new Suite(ASTConverterBugsTestJLS4.class.getName());
	List tests = buildTestsList(ASTConverterBugsTestJLS4.class, 2, 0/* do not sort*/);
	for (int index=0, size=tests.size(); index<size; index++) {
		suite.addTest((Test)tests.get(index));
	}
	return suite;
}

}
