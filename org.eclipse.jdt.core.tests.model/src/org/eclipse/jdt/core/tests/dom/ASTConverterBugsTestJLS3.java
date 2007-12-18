/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.dom.AST;

public class ASTConverterBugsTestJLS3 extends ASTConverterBugsTest {

public ASTConverterBugsTestJLS3(String name) {
    super(name);
    this.testLevel = AST.JLS3;
}

public static Test suite() {
	TestSuite suite = new Suite(ASTConverterBugsTestJLS3.class.getName());
	List tests = buildTestsList(ASTConverterBugsTestJLS3.class, 1, 0/* do not sort*/);
	for (int index=0, size=tests.size(); index<size; index++) {
		suite.addTest((Test)tests.get(index));
	}
	return suite;
}

}
