/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import org.eclipse.jdt.internal.core.builder.*;

public class FriendDependencyTests {

// this is a compilation only test to verify that this method still exists since API Tooling is using it
public void includes(ReferenceCollection r) {
	char[][][] qualifiedNames = null;
	char[][] simpleNames = null;
	char[][] rootNames = null;
	r.includes(qualifiedNames, simpleNames, rootNames);
}

// this is a compilation only test to verify that this method still exists since API Tooling is using it
public void internSimpleNames() {
	ReferenceCollection.internSimpleNames(new StringSet(1), true);
}
}
