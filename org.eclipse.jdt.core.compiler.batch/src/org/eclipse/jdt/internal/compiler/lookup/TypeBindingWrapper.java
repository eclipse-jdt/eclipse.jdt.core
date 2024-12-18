package org.eclipse.jdt.internal.compiler.lookup;

interface TypeBindingWrapper {
	void swapUnresolved(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType, LookupEnvironment env);
}
