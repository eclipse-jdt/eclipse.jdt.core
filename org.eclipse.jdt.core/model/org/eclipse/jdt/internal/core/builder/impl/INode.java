package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.IType;

import java.util.Vector;

/**
 * A node in the image builder dependency graph.
 * @see IDependencyGraph
 */
public interface INode {
	// kinds of nodes (mutually exclusive, so they don't need to be maskable)
	public static final int JCU_NODE = 0;
	public static final int TYPE_NODE = 1;
	public static final int NAMESPACE_NODE = 2;
	public static final int ZIP_NODE = 3;
	/**
	 * Adds a node that depends on this node.
	 */
	void addDependent(INode nodeThatDependsOnMe);
	/**
	 * Clears all incoming and outgoing dependency links for this node
	 */
	void clearDependencies();
	/**
	 * Adds a node that depends on this node.  
	 * IMPORTANT: This should only be called by dependency graph copy methods.
	 * The dependents array at this point contains either new dependents or null
	 * values.  This method must find the first null entry in the dependents array
	 * and add the given value.
	 */
	void copyAddDependent(INode nodeThatDependsOnMe);
	/**
	 * Returns an enumeration over the nodes that this node depends on.  
	 * A change to the prinicipal structure of any of these nodes may affect 
	 * the principal structure of this node.
	 */
	INode[] getDependencies();
	/**
	 * Returns an enumeration over the nodes that depend on this node.  
	 * A change to the prinicipal structure of this node may affect the principal
	 * structure of the returned dependents.
	 */
	INode[] getDependents();
	/**
	 * Returns the element which this node represents.
	 */
	public Object getElement();
	/**
	 * Returns what kind of node this is.
	 */
	int getKind();
	/**
	 * Returns the topological order number of this node.
	 * Note that this may change if direct or indirect predecessors 
	 * are added or removed from the graph.
	 */
	int getOrder();
	/**
	 * Returns the types that belong to this node
	 */
	IType[] getTypes();
	/**
	 * Removes a node on which this node depends.
	 * This -does- remove the backwards link from the other node.
	 */
	void removeDependency(INode nodeThatIDependOn);
	/**
	 * Removes a node that depends on this node
	 */
	void removeDependent(INode nodeThatDependsOnMe);
	/**
	 * Sets the nodes that this node depends on.
	 */
	void setDependencies(INode[] nodesThatIDependOn);
}
