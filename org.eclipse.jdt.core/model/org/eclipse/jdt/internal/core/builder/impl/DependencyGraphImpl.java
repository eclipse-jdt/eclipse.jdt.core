package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;

/**
 * @see IDependencyGraph
 */
public class DependencyGraphImpl implements IDependencyGraph {
	StateImpl fState;
/**
 * DependencyGraphImpl constructor comment.
 */
DependencyGraphImpl(StateImpl state) {
	fState = state;
}
	/**
	 * @see IDependencyGraph#getNamespaceDependencies
	 */
	public IPackage[] getNamespaceDependencies(IType type) {
		Assert.isTrue(!type.isStateSpecific());
		TypeStructureEntry tsEntry = fState.buildTypeStructureEntry(type);
		Object key = tsEntry.getDependencyGraphKey();
		return fState.getInternalDependencyGraph().getNamespaceDependencies(key);
	}
	/**
	 * @see IDependencyGraph#getState
	 */
	public IState getState() {
		return fState;
	}
	/**
	 * @see IDependencyGraph#getTypeDependencies
	 */
	public IType[] getTypeDependencies(IType type) {
		Assert.isTrue(!type.isStateSpecific());
		TypeStructureEntry tsEntry = fState.buildTypeStructureEntry(type);
		Object key = tsEntry.getDependencyGraphKey();
		return fState.getInternalDependencyGraph().getTypeDependencies(key);
	}
}
