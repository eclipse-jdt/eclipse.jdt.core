/*******************************************************************************
 * Copyright (c) 2000-2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.ClassFile;

/*
 * A name environment that wraps another one and that look in a list of potential matches before
 * looking at the wrapped name environment.
 */
public class JavaSearchNameEnvironment2 implements INameEnvironment {
	
	INameEnvironment nameEnvironment;
	PotentialMatch[] potentialMatches;
	
public JavaSearchNameEnvironment2(INameEnvironment nameEnvironment, PotentialMatch[] potentialMatches) {
	this.nameEnvironment = nameEnvironment;
	this.potentialMatches = potentialMatches;
}

public void cleanup() {
	this.nameEnvironment.cleanup();
}

public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
	if (typeName == null) return null;
	char[][] compoundName = CharOperation.arrayConcat(packageName, typeName);
	NameEnvironmentAnswer answer = findTypeInPotentialMatches(compoundName);
	if (answer != null) return answer;
	return this.nameEnvironment.findType(typeName, packageName);
}

public NameEnvironmentAnswer findType(char[][] compoundName) {
	if (compoundName == null) return null;
	NameEnvironmentAnswer answer = findTypeInPotentialMatches(compoundName);
	if (answer != null) return answer;
	return this.nameEnvironment.findType(compoundName);
}
private NameEnvironmentAnswer findTypeInPotentialMatches(char[][] compoundName) {
	for (int i = 0, length = this.potentialMatches.length; i < length; i++) {
		PotentialMatch potentialMatch = this.potentialMatches[i];
		if (potentialMatch != null && CharOperation.equals(potentialMatch.compoundName, compoundName)) {
			if (potentialMatch.openable instanceof ClassFile && potentialMatch.getContents() == CharOperation.NO_CHAR) {
				return null;
			} else {
				return new NameEnvironmentAnswer(potentialMatch);
			}
		}
	}
	return null;
}

public boolean isPackage(char[][] compoundName, char[] packageName) {
	return this.nameEnvironment.isPackage(compoundName, packageName);
}

}
