/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.util.ArrayList;

import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.util.HashtableOfLong;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.util.SimpleSet;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * A set of matches and possible matches, which need to be resolved.
 */
public class MatchingNodeSet {

/**
 * Map of matching ast nodes that don't need to be resolved to their accuracy level.
 * Each node is removed as it is reported.
 */
SimpleLookupTable matchingNodes = new SimpleLookupTable(3);
private HashtableOfLong matchingNodesKeys = new HashtableOfLong(3);
static Integer EXACT_MATCH = new Integer(SearchMatch.A_ACCURATE);
static Integer POTENTIAL_MATCH = new Integer(SearchMatch.A_INACCURATE);

/**
 * Set of possible matching ast nodes. They need to be resolved
 * to determine if they really match the search pattern.
 */
SimpleSet possibleMatchingNodesSet = new SimpleSet(7);
private HashtableOfLong possibleMatchingNodesKeys = new HashtableOfLong(7);

public int addMatch(ASTNode node, int matchLevel) {
	switch (matchLevel) {
		case PatternLocator.INACCURATE_MATCH:
			addTrustedMatch(node, false);
			break;
		case PatternLocator.POSSIBLE_MATCH:
			addPossibleMatch(node);
			break;
		case PatternLocator.ACCURATE_MATCH:
			addTrustedMatch(node, true);
	}
	return matchLevel;
}
public void addPossibleMatch(ASTNode node) {
	// remove existing node at same position from set
	// (case of recovery that created the same node several time
	// see http://bugs.eclipse.org/bugs/show_bug.cgi?id=29366)
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	ASTNode existing = (ASTNode) this.possibleMatchingNodesKeys.get(key);
	if (existing != null && existing.getClass().equals(node.getClass()))
		this.possibleMatchingNodesSet.remove(existing);

	// add node to set
	this.possibleMatchingNodesSet.add(node);
	this.possibleMatchingNodesKeys.put(key, node);
}
public void addTrustedMatch(ASTNode node, boolean isExact) {
	// remove existing node at same position from set
	// (case of recovery that created the same node several time
	// see http://bugs.eclipse.org/bugs/show_bug.cgi?id=29366)
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	ASTNode existing = (ASTNode) this.matchingNodesKeys.get(key);
	if (existing != null && existing.getClass().equals(node.getClass()))
		this.matchingNodes.removeKey(existing);
	
	// map node to its accuracy level
	this.matchingNodes.put(node, isExact ? EXACT_MATCH : POTENTIAL_MATCH);
	this.matchingNodesKeys.put(key, node);
}
protected boolean hasPossibleNodes(int start, int end) {
	Object[] nodes = this.possibleMatchingNodesSet.values;
	for (int i = 0, l = nodes.length; i < l; i++) {
		ASTNode node = (ASTNode) nodes[i];
		if (node != null && start <= node.sourceStart && node.sourceEnd <= end)
			return true;
	}
	nodes = this.matchingNodes.keyTable;
	for (int i = 0, l = nodes.length; i < l; i++) {
		ASTNode node = (ASTNode) nodes[i];
		if (node != null && start <= node.sourceStart && node.sourceEnd <= end)
			return true;
	}
	return false;
}
/**
 * Returns the matching nodes that are in the given range in the source order.
 */
protected ASTNode[] matchingNodes(int start, int end) {
	ArrayList nodes = null;
	Object[] keyTable = this.matchingNodes.keyTable;
	for (int i = 0, l = keyTable.length; i < l; i++) {
		ASTNode node = (ASTNode) keyTable[i];
		if (node != null && start <= node.sourceStart && node.sourceEnd <= end) {
			if (nodes == null) nodes = new ArrayList();
			nodes.add(node);
		}
	}
	if (nodes == null) return null;

	ASTNode[] result = new ASTNode[nodes.size()];
	nodes.toArray(result);

	// sort nodes by source starts
	Util.Comparer comparer = new Util.Comparer() {
		public int compare(Object o1, Object o2) {
			return ((ASTNode) o1).sourceStart - ((ASTNode) o2).sourceStart;
		}
	};
	Util.sort(result, comparer);
	return result;
}
public Object removePossibleMatch(ASTNode node) {
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	ASTNode existing = (ASTNode) this.possibleMatchingNodesKeys.get(key);
	if (existing == null) return null;

	this.possibleMatchingNodesKeys.put(key, null);
	return this.possibleMatchingNodesSet.remove(node);
}
public Object removeTrustedMatch(ASTNode node) {
	long key = (((long) node.sourceStart) << 32) + node.sourceEnd;
	ASTNode existing = (ASTNode) this.matchingNodesKeys.get(key);
	if (existing == null) return null;

	this.matchingNodesKeys.put(key, null);
	return this.matchingNodes.removeKey(node);
}
public String toString() {
	StringBuffer result = new StringBuffer();
	result.append("Exact matches:"); //$NON-NLS-1$
	Object[] keyTable = this.matchingNodes.keyTable;
	Object[] valueTable = this.matchingNodes.valueTable;
	for (int i = 0, l = keyTable.length; i < l; i++) {
		ASTNode node = (ASTNode) keyTable[i];
		if (node == null) continue;
		result.append("\n\t"); //$NON-NLS-1$
		result.append(valueTable[i] == EXACT_MATCH
			? "ACCURATE_MATCH: " //$NON-NLS-1$
			: "INACCURATE_MATCH: "); //$NON-NLS-1$
		node.print(0, result);
	}

	result.append("\nPossible matches:"); //$NON-NLS-1$
	Object[] nodes = this.possibleMatchingNodesSet.values;
	for (int i = 0, l = nodes.length; i < l; i++) {
		ASTNode node = (ASTNode) nodes[i];
		if (node == null) continue;
		result.append("\nPOSSIBLE_MATCH: "); //$NON-NLS-1$
		node.print(0, result);
	}
	return result.toString();
}
}
