package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.builder.IType;
import org.eclipse.jdt.internal.core.util.Dumper;
import org.eclipse.jdt.internal.core.util.IDumpable;

/**
 * A node in the image builder dependency graph.
 * @see IDependencyGraph
 */
public abstract class AbstractNode implements INode, IDumpable, Cloneable {

	/**
	 * The "order number" of the node.  
	 * Nodes with lower order numbers are earlier
	 * in the topological order.
	 * An order of -1 indicates that the order has not yet been calculated.
	 */
	protected int fOrder = -1;

	/**
	 * Nodes that this node depends on
	 */
	protected INode[] fDependencies = fgEmptyNodeList;

	/**
	 * Nodes that depend on this node
	 */
	protected INode[] fDependents = fgEmptyNodeList;
	protected int fNumDependents = 0;

	/**
	 * Default list of dependencies or dependents
	 */
	protected static final INode[] fgEmptyNodeList = new INode[0];
	/**
	 * Hide the default constructor to force specialized instantiation of subclasses
	 */
	protected AbstractNode() {
	}
	/**
	 * Adds a node that depends on this node.  
	 * IMPORTANT: This should only be called by addDependency.
	 */
	public void addDependent(INode nodeThatDependsOnMe) {
		/* check if I already have this dependent */
/*		
		for (int i = fNumDependents; --i >= 0;) {
			if (fDependents[i] == nodeThatDependsOnMe) {
				System.out.println("Attempt to add duplicate dependent " + nodeThatDependsOnMe.getElement() + " to " + getElement());
				return;
			}
		}
*/		
		if (fNumDependents >= fDependents.length) {
			/* grow array */
			INode[] newDependents = new INode[fNumDependents == 0 ? 5 : fNumDependents*2+1];
			System.arraycopy(fDependents, 0, newDependents, 0, fNumDependents);
			fDependents = newDependents;
		}
		fDependents[fNumDependents++] = nodeThatDependsOnMe;
	}
	/**
	 * Clears all outgoing dependency links for this node
	 */
	public void clearDependencies() {

		/* do for all my dependencies (Nodes that I depend on) */
		INode[] dependencies = getDependencies();
		for (int i = 0; i < dependencies.length; i++) {
			dependencies[i].removeDependent(this);
		}
		fDependencies = fgEmptyNodeList;
		invalidateOrder();
	}
	/**
	 * Returns a copy of this node, without copying dependencies.  Used
	 * by DependencyGraph.copy().
	 */
	public abstract AbstractNode copy();
	/**
	 * Adds a node that depends on this node.  
	 * IMPORTANT: This should only be called by dependency graph copy methods.
	 * The dependents array at this point contains either new dependents or null
	 * values.  This method must find the first null entry in the dependents array
	 * and add the given value.
	 */
	public void copyAddDependent(INode nodeThatDependsOnMe) {
		/* find first null entry */
		int min = 0;
		int max = fNumDependents - 1;
		int mid = (max + min) / 2;
		boolean found = false;

		/* binary search until on or next to first non-null entry */
try {
		while (max > min) {
			if (fDependents[mid] == null) {
				/* look in lower half */
				max = mid - 1;
			} else {
				/* look in higher half */
				min = mid + 1;
			}
			mid = (max + min) / 2;
		}

		/* linear search for first non-null entry */
		while (fDependents[min] != null) {
			min++;
		}

		/* add the dependent */
		fDependents[min] = nodeThatDependsOnMe;
} catch (ArrayIndexOutOfBoundsException e) {
	System.out.println("ArrayIndexOutOfBoundsException in AbstractNode.copyAddDependent()"/*nonNLS*/);
}
	}
	/**
	 * Returns a copy of this node, but with the dependents and dependencies
	 * left unchanged.  They are later replaced using replaceDeps().
	 * Be careful that this implementation applies to any information in subclasses.
	 * Right now, all information except the dependents are non-state-specific
	 * and can be shared.
	 */
	INode copyWithoutReplacingDeps() {
		AbstractNode newNode = this.copy();

		/* copy dependencies into new array */
		int depCount = fDependencies.length;
		newNode.fDependencies = new INode[depCount];
		for (int i = depCount; --i >= 0;) {
			newNode.fDependencies[i] = fDependencies[i];
		}
		
		newNode.fNumDependents = fNumDependents;
		newNode.fDependents = new INode[fNumDependents];
		newNode.fOrder = fOrder;
		return newNode;
	}
/**
 * For debugging only. 
 */
public void dump(Dumper dumper) {
	dumper.dump("element"/*nonNLS*/, getElement());
	if (getKind() == JCU_NODE) 
		dumper.dump("types"/*nonNLS*/, getTypes());
		
	Object[] dependencies = new Object[fDependencies.length];
	for (int i = 0; i < fDependencies.length; ++i) {
		dependencies[i] = fDependencies[i].getElement();
	}
	dumper.dump("dependencies"/*nonNLS*/, dependencies);
		
	Object[] dependents = new Object[fNumDependents];
	for (int i = 0; i < fDependents.length; ++i) {
		dependents[i] = fDependents[i].getElement();
	}
	dumper.dump("dependents"/*nonNLS*/, dependents);
	
}
	/**
	 * Make sure equality tests are never carried out on nodes
	 */
	public boolean equals(Object o) {
		Assert.isTrue(false, Util.bind("build.noEqualityForNodes"/*nonNLS*/));
		return false;
	}
	/**
	 * Returns the nodes that this node depends on.  A change to the principal structure
	 * of any of these nodes may affect the principal structure of this node.
	 */
	public INode[] getDependencies() {
		return fDependencies;
	}
	/**
	 * Returns the nodes that depend on this node.  A change to the principal structure
	 * of this node may affect the principal structure of the returned dependents.
	 */
	public INode[] getDependents() {
		if (fNumDependents < fDependents.length) {
			System.arraycopy(fDependents, 0, fDependents = new INode[fNumDependents], 0, fNumDependents);
		}
		return fDependents;
	 }
	/**
	 * Returns the number of bytes that this node uses.
	 * For debugging and profiling purposes only.
	 */
	int getFootprint() {
		/* 5 slots plus 8 bytes for object header */
		int size = 28;

		/* size of dep arrays */
		if (fDependencies != null) {
			size += fDependencies.length * 4;
		}
		if (fDependents != null) {
			size += fDependents.length * 4;
		}
		return size;
	}
	/**
	 * @see INode
	 */
	public int getOrder() {
		return getOrder(0);
	}
	/**
	 * Returns the order number, calculating it if not already done.
	 * seen is a set of previously visited nodes.
	 */
	private int getOrder(int recursionLevel) {
		if (fOrder != -1) {
			return fOrder;
		}

//		trace(recursionLevel, -1);

		fOrder = 0;  // Mark as non-invalid to break cycles
		int max = -1;
		for (int i = 0, len = fDependencies.length; i < len; ++i) {
			int order = ((AbstractNode) fDependencies[i]).getOrder(recursionLevel + 1);
			if (order > max) {
				max = order;
			}
		}
		fOrder = max + 1;
		
//		trace(recursionLevel, fOrder);
		return fOrder;
	}
	/**
	 * Returns the types that belong to this node
	 */
	public abstract IType[] getTypes();
	
	/**
	 * Invalidates the order number, for this node and all dependents.
	 */
	public void invalidateOrder() {
		if (fOrder != -1) {
			fOrder = -1;
			/* do for each node that depends on me */
			for (int i = 0, len = fNumDependents; i < len; ++i) {
				((AbstractNode) fDependents[i]).invalidateOrder();
			}
		}
	}
	/**
	 * Removes a node on which this node depends.
	 * This -does- remove the backwards link from the other node.
	 */
	public void removeDependency(INode nodeThatIDependOn) {
		nodeThatIDependOn.removeDependent(this);
		int size = fDependencies.length;
		for (int i = size; --i >= 0;) {
			if (fDependencies[i] == nodeThatIDependOn) {
				/* shrink array */
				if (--size == 0) {
					fDependencies = fgEmptyNodeList;
				} else {
					INode[] newDependencies = new INode[size];
					System.arraycopy(fDependencies, 0, newDependencies, 0, i);
					System.arraycopy(fDependencies, i + 1, newDependencies, i, size - i);
					fDependencies = newDependencies;
				}
				invalidateOrder();
				return;
			}
		}
	}
	/**
	 * Removes a node that depends on this node.
	 * This does not remove the backwards link.
	 */
	public void removeDependent(INode nodeThatDependsOnMe) {
		int size = fNumDependents;
		for (int i = size; --i >= 0;) {
			if (fDependents[i] == nodeThatDependsOnMe) {
				/* shrink array */
				if (--size == 0) {
					fDependents = fgEmptyNodeList;
				} else {
					INode[] newDependents = new INode[size];
					System.arraycopy(fDependents, 0, newDependents, 0, i);
					System.arraycopy(fDependents, i + 1, newDependents, i, size - i);
					fDependents = newDependents;
				}
				fNumDependents = size;
				return;
			}
		}
	}
	/**
	 * This node has previously been copied without updating its
	 * dependency references.  Now replace the dependency references
	 * with the corresponding nodes in the given graph.
	 * Note that we only need to replace the dependencies, because the dependents
	 * are added as backward links from other nodes.
	 */
	void replaceDeps(DependencyGraph graph) {
		int depCount = fDependencies.length;
		INode newNode;

		for (int i = 0; i < depCount; i++) {
			/* get new node for old node */
			newNode = graph.getNodeFor(fDependencies[i]);
			newNode.copyAddDependent(this);
			fDependencies[i] = newNode;
		}
	 }
	/**
	 * Sets the nodes that this node depends on.  
	 * Backwards links are added automatically.
	 */
	public void setDependencies(INode[] nodesThatIDependOn) {

		/* clear old dependencies before nuking array */
		clearDependencies();
		fDependencies = nodesThatIDependOn;

		/* add backwards links */
		for (int i = nodesThatIDependOn.length; --i >= 0;) {
			nodesThatIDependOn[i].addDependent(this);
		}

		invalidateOrder();
	}
	public void trace(int recursionLevel, int refCount) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < recursionLevel; ++i)
			sb.append(' ');
		sb.append(this);
		sb.append(": "/*nonNLS*/);
		sb.append(refCount);
		System.out.println(sb.toString());
	}
}
