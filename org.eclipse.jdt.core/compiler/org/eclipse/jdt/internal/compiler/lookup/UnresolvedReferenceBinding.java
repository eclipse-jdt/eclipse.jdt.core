package org.eclipse.jdt.internal.compiler.lookup;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.util.*;

public class UnresolvedReferenceBinding extends ReferenceBinding {
	ReferenceBinding resolvedType;
UnresolvedReferenceBinding(char[][] compoundName, PackageBinding packageBinding) {
	this.compoundName = compoundName;
	this.fPackage = packageBinding;
}
String debugName() {
	return toString();
}
ReferenceBinding resolve(LookupEnvironment environment) {
	if (resolvedType != null)
		return resolvedType;

	ReferenceBinding environmentType;
	if ((environmentType = environment.askForType(compoundName)) != null) {
		if (environmentType != this){ // could not resolve any better, error was already reported against it
			resolvedType = environmentType;			
			environment.updateArrayCache(this, environmentType);
			return environmentType; // when found, it replaces the unresolved type in the cache
		}
	}

	environment.problemReporter.isClassPathCorrect(compoundName, null);
	return null; // will not get here since the above error aborts the compilation
}
public String toString() {
	return "Unresolved type " + ((compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED"); //$NON-NLS-1$ //$NON-NLS-2$
}
}
