/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMPackageReferenceLocator extends DOMPatternLocator {

	private PackageReferenceLocator locator;

	public DOMPackageReferenceLocator(PackageReferenceLocator locator) {
		super(locator.pattern);
		this.locator = locator;
	}

	@Override
	public LocatorResponse match(Name node, NodeSetWrapper nodeSet, MatchLocator locator) {
		return toResponse(matchesName(this.locator.pattern.pkgName, node.getFullyQualifiedName().toCharArray()) ? POSSIBLE_MATCH :IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse resolveLevel(org.eclipse.jdt.core.dom.ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding instanceof IPackageBinding ipb) {
			String n = ipb.getName();
			String patternName = new String(this.locator.pattern.pkgName);
			if (patternName.equals(n)) {
				return toResponse(ACCURATE_MATCH);
			}
		}
		if (binding == null) {
			return toResponse(INACCURATE_MATCH);
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

}
