package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

import java.util.*;

/**
 * 
 */
public class JCUNode extends AbstractNode {
	/* The compilation unit */
	PackageElement fUnit;

	/* The types that belong to the compilation unit */
	IType[] fTypes;

	/* empty types list */
	protected static final IType[] fgNoTypes = new IType[0];
	public JCUNode (PackageElement unit) {
		fUnit = unit;
		fTypes = fgNoTypes;
	}
	public JCUNode (PackageElement unit, IType[] types) {
		fUnit = unit;

		/* never let types be null */
		fTypes = types == null ? fgNoTypes : types;
	}
	/**
	 * Returns a copy of this node, without copying dependencies.  Used
	 * by DependencyGraph.copy().
	 */
	public AbstractNode copy() {
		return new JCUNode(fUnit, fTypes);
	}
	/**
	 * Returns the element which this node represents.
	 */
	public Object getElement() {
		return fUnit;
	}
	/**
	 * Returns the number of bytes that this node uses.
	 * For debugging and profiling purposes only.
	 */
	int getFootprint() {
		int size = super.getFootprint();

		/* slot for package element */
		size += 4;

		/* slots for types */
		if (fTypes != null) {
			size += fTypes.length * 4;
		}
		return size;
	}
	/**
	 * Returns what kind of node this is.
	 */
	public int getKind() {
		return JCU_NODE;
	}
	public PackageElement getPackageElement() {
		return fUnit;
	}
	/**
	 * Returns the types that belong to this compilation unit
	 */
	public IType[] getTypes() {
		return fTypes;
	}
	/**
	 * Sets the types that belong to this compilation unit
	 */
	public void setTypes(IType[] types) {
		/* never let types be null */
		fTypes = types == null ? fgNoTypes : types;
	}
	/**
	 * Prints a string representation of the node.  This method is for debugging
	 * purposes only.
	 */
	public String toString() {
		return "JCUNode("/*nonNLS*/ + fUnit.getFileName() + ")"/*nonNLS*/;
	}
}
