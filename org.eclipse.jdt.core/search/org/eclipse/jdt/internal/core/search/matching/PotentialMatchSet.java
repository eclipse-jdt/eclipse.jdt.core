/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

/**
 * A set of PotentialMatches that is sorted by package fragment roots.
 */
public class PotentialMatchSet {

private SimpleLookupTable rootsToPotentialMatches = new SimpleLookupTable(5);
private int elementCount = 0;

public void add(PotentialMatch potentialMatch) {
	IPath path = potentialMatch.openable.getPackageFragmentRoot().getPath();
	ObjectVector potentialMatches = (ObjectVector) this.rootsToPotentialMatches.get(path);
	if (potentialMatches != null) {
		if (potentialMatches.contains(potentialMatch)) return;
	} else {
		this.rootsToPotentialMatches.put(path, potentialMatches = new ObjectVector());
	}

	potentialMatches.add(potentialMatch);
	this.elementCount++;
}
public PotentialMatch[] getPotentialMatches(IPackageFragmentRoot[] roots) {
	PotentialMatch[] result = new PotentialMatch[this.elementCount];
	int index = 0;
	for (int i = 0, length = roots.length; i < length; i++) {
		ObjectVector potentialMatches = (ObjectVector) this.rootsToPotentialMatches.get(roots[i].getPath());
		if (potentialMatches != null) {
			potentialMatches.copyInto(result, index);
			index += potentialMatches.size();
		}
	}
	if (index < this.elementCount)
		System.arraycopy(result, 0, result = new PotentialMatch[index], 0, index);
	return result;
}
public void reset() {
	this.rootsToPotentialMatches = new SimpleLookupTable(5);
	this.elementCount = 0;
}
}
