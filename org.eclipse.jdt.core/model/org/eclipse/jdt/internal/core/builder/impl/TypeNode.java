package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.IType;

import java.util.*;


/**
 * Type nodes represent compiled types in the graph.  They are types
 * that were importing into the workspace with no source code available.
 * They cannot be compiled, but they still have dependents and dependencies.
 */
public class TypeNode extends AbstractNode {
	PackageElement fType;
	/**
	 * Creates a new type node with the given type
	 */
	public TypeNode(PackageElement type) {
		fType = type;
	}
	/**
	 * Returns a copy of this node, without copying dependencies.  Used
	 * by DependencyGraph.copy().
	 */
	public AbstractNode copy() {
		return new TypeNode(fType);
	}
	/**
	 * Returns the element which this node represents.
	 */
	public Object getElement() {
		return fType;
	}
	/**
	 * Returns the number of bytes that this node uses.
	 * For debugging and profiling purposes only.
	 */
	int getFootprint() {
		/* one slot for type */
		return super.getFootprint() + 4;
	}
	/**
	 * Returns what kind of node this is.
	 */
	public int getKind() {
		return TYPE_NODE;
	}
	public PackageElement getPackageElement() {
		return fType;
	}
public IType[] getTypes() {
	String fileName = fType.getFileName();
	int lastDot = fileName.lastIndexOf('.');
	IType type = fType.getPackage().getClassHandle(fileName.substring(0, lastDot));
	return new IType[] {type};
}
	/**
	 * Prints a string representation of the node.  This method is for debugging
	 * purposes only.
	 */
	public String toString() {
		return "TypeNode(" + fType.getFileName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
