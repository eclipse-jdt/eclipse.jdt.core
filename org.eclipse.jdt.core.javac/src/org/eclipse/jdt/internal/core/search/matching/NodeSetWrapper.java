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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;

public class NodeSetWrapper {
	private Set<org.eclipse.jdt.core.dom.ASTNode> possibleASTNodes = new LinkedHashSet<>();
	public final Map<org.eclipse.jdt.core.dom.ASTNode, Integer> trustedASTNodeLevels = new LinkedHashMap<>();
	private boolean mustResolve;
	public NodeSetWrapper(boolean mustResolve) {
		this.setMustResolve(mustResolve);
	}

	public boolean mustResolve() {
		return mustResolve;
	}

	public void setMustResolve(boolean mustResolve) {
		this.mustResolve = mustResolve;
	}

	public int addMatch(org.eclipse.jdt.core.dom.ASTNode node, int matchLevel) {
		int maskedLevel = matchLevel & PatternLocator.MATCH_LEVEL_MASK;
		switch (maskedLevel) {
			case PatternLocator.INACCURATE_MATCH:
				if (matchLevel != maskedLevel) {
					addTrustedMatch(node, Integer.valueOf(SearchMatch.A_INACCURATE+(matchLevel & PatternLocator.FLAVORS_MASK)));
				} else {
					addTrustedMatch(node, MatchingNodeSet.POTENTIAL_MATCH);
				}
				break;
			case PatternLocator.POSSIBLE_MATCH:
				addPossibleMatch(node);
				break;
			case PatternLocator.ERASURE_MATCH:
				if (matchLevel != maskedLevel) {
					addTrustedMatch(node, Integer.valueOf(SearchPattern.R_ERASURE_MATCH+(matchLevel & PatternLocator.FLAVORS_MASK)));
				} else {
					addTrustedMatch(node, MatchingNodeSet.ERASURE_MATCH);
				}
				break;
			case PatternLocator.ACCURATE_MATCH:
				if (matchLevel != maskedLevel) {
					addTrustedMatch(node, Integer.valueOf(SearchMatch.A_ACCURATE+(matchLevel & PatternLocator.FLAVORS_MASK)));
				} else {
					addTrustedMatch(node, MatchingNodeSet.EXACT_MATCH);
				}
				break;
		}
		return matchLevel;
	}
	public void addPossibleMatch(org.eclipse.jdt.core.dom.ASTNode node) {
		this.possibleASTNodes.add(node);
	}
	public void addTrustedMatch(org.eclipse.jdt.core.dom.ASTNode node, boolean isExact) {
		addTrustedMatch(node, isExact ? MatchingNodeSet.EXACT_MATCH : MatchingNodeSet.POTENTIAL_MATCH);
	}
	void addTrustedMatch(org.eclipse.jdt.core.dom.ASTNode node, Integer level) {
		this.trustedASTNodeLevels.put(node, level);
	}
	int getTrustedMatch(org.eclipse.jdt.core.dom.ASTNode node) {
		Integer i = this.trustedASTNodeLevels.get(node);
		return i == null ? 0 : i.intValue();
	}

}