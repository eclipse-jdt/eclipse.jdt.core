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

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;

/**
 * A set of PotentialMatches that is sorted by package fragment roots.
 */
public class PotentialMatchSet {
	private HashtableOfObject rootsToPotentialMatches = new HashtableOfObject(5);
	private int elementCount = 0;
	
	public void add(PotentialMatch potentialMatch) {
		IPackageFragmentRoot root = potentialMatch.openable.getPackageFragmentRoot();
		char[] path = root.getPath().toString().toCharArray();
		ObjectVector potentialMatches = (ObjectVector)this.rootsToPotentialMatches.get(path);
		if (potentialMatches == null) {
			potentialMatches = new ObjectVector();
			this.rootsToPotentialMatches.put(path, potentialMatches);
			potentialMatches.add(potentialMatch);
			this.elementCount++;
		} else if (!potentialMatches.contains(potentialMatch)) {
			potentialMatches.add(potentialMatch);
			this.elementCount++;
		}
	}
	
	public PotentialMatch[] getPotentialMatches(IPackageFragmentRoot[] roots) {
		PotentialMatch[] result = new PotentialMatch[this.elementCount];
		int index = 0;
		for (int i = 0, length = roots.length; i < length; i++) {
			IPackageFragmentRoot root = roots[i];
			char[] path = root.getPath().toString().toCharArray();
			ObjectVector potentialMatches = (ObjectVector)this.rootsToPotentialMatches.get(path);
			if (potentialMatches != null) {
				potentialMatches.copyInto(result, index);
				index += potentialMatches.size();
			}
		}
		if (index < this.elementCount) {
			System.arraycopy(
				result, 
				0, 
				result = new PotentialMatch[index],
				0,
				index);
		}
		return result;
	}
}

