package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.IPackage;

/**
 * 
 */
public class NamespaceNode extends AbstractNode {
	protected IPackage fPackage;
	/**
	 * Creates a new namespace node for the given package
	 */
	public NamespaceNode(IPackage pkg) {
		fPackage = pkg;
	}

	/**
	 * Adds a node that this node depends on.  Backwards link is added automatically
	 */
	public void addDependency(INode nodeThatIDependOn) {
		Assert.isTrue(false, "namespaces cannot have dependencies");
	}

	/**
	 * Returns a copy of this node, without copying dependencies.  Used
	 * by DependencyGraph.copy().
	 */
	public AbstractNode copy() {
		return new NamespaceNode(fPackage);
	}

	/**
	 * Returns the element which this node represents.
	 */
	public Object getElement() {
		return fPackage;
	}

	/**
	 * Returns the number of bytes that this node uses.
	 * For debugging and profiling purposes only.
	 */
	int getFootprint() {
		/* one slot for package */
		return super.getFootprint() + 4;
	}

	/**
	 * Returns what kind of node this is.
	 */
	public int getKind() {
		return NAMESPACE_NODE;
	}

	public IPackage getPackage() {
		return fPackage;
	}

	/**
	 * Prints a string representation of the node.  This method is for debugging
	 * purposes only.
	 */
	public String toString() {
		return "NamespaceNode(" + fPackage.getName() + ")";
	}

}
