package org.eclipse.jdt.internal.core.builder;

public interface IDependencyGraph {
	/**
	 * Returns the namespaces on which the given type depends.
	 */
	IPackage[] getNamespaceDependencies(IType type);
	/**
	 * Returns the state whose dependencies this graph describes.
	 */
	IState getState();
	/**
	 * Returns the types on which the given type depends, if known, or null if not known.
	 */
	IType[] getTypeDependencies(IType type);
}
