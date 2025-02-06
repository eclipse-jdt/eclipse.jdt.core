/*******************************************************************************
 * Copyright (c) 2025 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.search.IJavaSearchDelegate;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

public class TestJavaSearchDelegate implements IJavaSearchDelegate {

	public TestJavaSearchDelegate() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void locateMatches(MatchLocator locator, IJavaProject javaProject, PossibleMatch[] possibleMatches,
			int start, int length) throws CoreException {
		// do nothing, stub
	}

}
