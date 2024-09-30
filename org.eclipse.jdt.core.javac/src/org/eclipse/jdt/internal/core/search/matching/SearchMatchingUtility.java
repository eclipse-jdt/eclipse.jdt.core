/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;

/*
 * Hold Utility methods for package-private functionality
 */
public class SearchMatchingUtility {
	public static void reportSearchMatch(MatchLocator locator, SearchMatch match) throws CoreException {
		locator.report(match);
	}
}
