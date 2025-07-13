/*******************************************************************************
 * Copyright (c) 2025 Red Hat Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ExportsDirective;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.OpensDirective;
import org.eclipse.jdt.core.dom.RequiresDirective;
import org.eclipse.jdt.internal.core.search.LocatorResponse;

public class DOMModuleLocator extends DOMPatternLocator {

	private final ModulePattern pattern;

	public DOMModuleLocator(ModuleLocator trl, ModulePattern modulePattern) {
		super(modulePattern);
		this.pattern = modulePattern;
	}

	@Override
	public LocatorResponse match(ModuleDeclaration node, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (pattern.findDeclarations && matchesName(node.getName().toString().toCharArray(), pattern.name)) {
			return toResponse(POSSIBLE_MATCH);
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse match(Name name, NodeSetWrapper nodeSet, MatchLocator locator) {
		if (pattern.findReferences
			&& (name.getLocationInParent() == RequiresDirective.NAME_PROPERTY
			    || name.getLocationInParent() == ExportsDirective.MODULES_PROPERTY
			    || name.getLocationInParent() == OpensDirective.MODULES_PROPERTY)
			&& matchesName(name.toString().toCharArray(), pattern.name)) {
			return toResponse(POSSIBLE_MATCH);
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}

	@Override
	public LocatorResponse resolveLevel(ASTNode node, IBinding binding, MatchLocator locator) {
		if (binding instanceof IModuleBinding mod && matchesName(mod.getName().toCharArray(), pattern.name)) {
			return toResponse(ACCURATE_MATCH);
		}
		return toResponse(IMPOSSIBLE_MATCH);
	}
}
