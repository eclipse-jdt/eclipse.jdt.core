package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * The dependency graph describes the dependencies between elements in a given state.
 * It is navigated using non-state-specific class or interface handles.
 */
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
