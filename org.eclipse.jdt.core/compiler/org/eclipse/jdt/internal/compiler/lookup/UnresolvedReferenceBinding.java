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
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;

public class UnresolvedReferenceBinding extends ReferenceBinding {
	ReferenceBinding resolvedType;
UnresolvedReferenceBinding(char[][] compoundName, PackageBinding packageBinding) {
	this.compoundName = compoundName;
	this.sourceName = compoundName[compoundName.length - 1]; // reasonable guess
	this.fPackage = packageBinding;
}
String debugName() {
	return toString();
}
ReferenceBinding resolve(LookupEnvironment environment) {
	if (resolvedType != null) return resolvedType;

	ReferenceBinding environmentType = fPackage.getType0(compoundName[compoundName.length - 1]);
	if (environmentType == this)
		environmentType = environment.askForType(compoundName);
	if (environmentType != null && environmentType != this) { // could not resolve any better, error was already reported against it
		resolvedType = environmentType;
		environment.updateArrayCache(this, environmentType);
		return environmentType; // when found, it replaces the unresolved type in the cache
	}

	environment.problemReporter.isClassPathCorrect(compoundName, null);
	return null; // will not get here since the above error aborts the compilation
}
public String toString() {
	return "Unresolved type " + ((compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED"); //$NON-NLS-1$ //$NON-NLS-2$
}
}
