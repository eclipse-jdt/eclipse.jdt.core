package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.util.*;

import java.util.*;

/**
 * An explicit graph of dependencies between source elements and packages
 * in the state.
 */
public class DependencyGraph implements IDumpable, Cloneable {
	/**
	 * Maps ICompilationUnits to INodes in the dependency graph
	 */
	private LookupTable fCompilationUnits;

	/**
	 * Maps IPackages to INodes in the dependency graph
	 */
	private LookupTable fNamespaces;

	/**
	 * Maps ITypes to INodes in the graph
	 */
	private LookupTable fTypes;

	/**
	 * Maps Strings (filenames) to INodes in the graph
	 */
	private LookupTable fZips;
	/**
	 * DependencyGraph constructor comment.
	 */
	public DependencyGraph() {
		fNamespaces = new LookupTable(11);
		fCompilationUnits = new LookupTable(11);
		fTypes = new LookupTable(11);
		fZips = new LookupTable(11);
	}

	/**
	 * Creates a node for the object.  Returns the added node.
	 */
	public INode add(Object element) {

		INode node = getNodeFor(element);
		// force creation of a node for the package
		if (element instanceof PackageElement) {
			getNodeFor(((PackageElement) element).getPackage());
		}
		return node;
	}

	/**
	 * Add the compilation unit, its dependencies, and any other relevant info 
	 * from the compilation result to the graph.
	 * The compilation unit has just been compiled, so its status is COMPILED.
	 */
	public void add(
		PackageElement resultUnit,
		IType[] types,
		Vector vDependencies) {

		// force the package node if not yet present
		getNodeFor(resultUnit.getPackage());

		JCUNode unitNode = (JCUNode) getNodeFor(resultUnit);
		unitNode.setTypes(types);
		INode node = unitNode;

		// convert dependencies to INodes
		INode[] deps = new INode[vDependencies.size()];
		int count = 0;
		for (Enumeration e = vDependencies.elements(); e.hasMoreElements();) {
			Object o = e.nextElement();
			INode depNode = getNodeFor(o);
			deps[count++] = depNode;
		}
		node.setDependencies(deps);
	}

	/**
	 * Returns a copy of the dependency graph with all counters and flags reset
	 */
	protected Object clone() {
		try {
			DependencyGraph newGraph = (DependencyGraph) super.clone();

			/* First pass: copy tables and all nodes, 
			 * leaving dependencies and dependents pointing to old nodes. */
			newGraph.fNamespaces = copyTableAndNodesWithoutReplacingDeps(fNamespaces);
			newGraph.fCompilationUnits =
				copyTableAndNodesWithoutReplacingDeps(fCompilationUnits);
			newGraph.fTypes = copyTableAndNodesWithoutReplacingDeps(fTypes);
			newGraph.fZips = copyTableAndNodesWithoutReplacingDeps(fZips);

			/* Second pass: replace dependencies and dependents to point to new nodes. */
			replaceDeps(newGraph.fNamespaces, newGraph);
			replaceDeps(newGraph.fCompilationUnits, newGraph);
			replaceDeps(newGraph.fTypes, newGraph);
			replaceDeps(newGraph.fZips, newGraph);

			return newGraph;
		} catch (CloneNotSupportedException e) {
			// Should not happen since we implement Cloneable
			Assert.isTrue(false, "Unexpected clone exception in DependencyGraph.clone()");
			return null;
		}
	}

	/**
	 * Returns a copy of the dependency graph with all counters and flags reset
	 */
	public DependencyGraph copy() {
		//this.integrityCheck();
		DependencyGraph newGraph = (DependencyGraph) this.clone();
		//newGraph.integrityCheck();
		return newGraph;
	}

	/**
	 * Copies the edges from an old node to a new node.  Any nodes that have
	 * to be created are added to the graph.
	 */
	protected void copyEdges(INode oldNode, INode newNode) {

		/* copy dependencies */
		INode[] oldDeps = oldNode.getDependencies();
		INode[] newDeps = new INode[oldDeps.length];

		/* create nodes in the new graph for each dependency in the old graph */
		for (int i = oldDeps.length; --i >= 0;) {
			newDeps[i] = getNodeFor(oldDeps[i]);
		}
		newNode.setDependencies(newDeps);

		/**
		 * Don't have to add dependents, because dependents
		 * will add newNode as a dependency, creating both directional links
		 */
	}

	/**
	 * Returns a copy of a table of nodes, with copies of the nodes,
	 * but with their dependencies and dependents not copied.
	 */
	private static LookupTable copyTableAndNodesWithoutReplacingDeps(LookupTable oldTable) {
		LookupTable newTable = (LookupTable) oldTable.clone();
		for (Enumeration e = oldTable.elements(); e.hasMoreElements();) {
			AbstractNode node = (AbstractNode) e.nextElement();
			newTable.put(node.getElement(), node.copyWithoutReplacingDeps());
		}
		return newTable;
	}

	/**
	 * Deletes the node in this graph corresponding to the given object,
	 * and answers it if found, or null if it wasn't found.
	 * The node must not have any dependents.
	 */
	public INode deleteNode(Object o) {
		INode toRemove = getNodeFor(o, false);
		if (toRemove != null) {
			deleteNode(toRemove);
		}
		return toRemove;
	}

	/**
	 * Deletes the node in this graph corresponding to the given object,
	 * and answers it if found, or null if it wasn't found.
	 * The node must not have any dependents.
	 */
	INode deleteNode(INode toRemove) {
		Assert.isTrue(toRemove.getDependents().length == 0);
		Object value = null;
		switch (toRemove.getKind()) {
			case INode.JCU_NODE :
				value = fCompilationUnits.remove(((JCUNode) toRemove).getPackageElement());
				break;
			case INode.TYPE_NODE :
				value = fTypes.remove(((TypeNode) toRemove).getPackageElement());
				break;
			case INode.NAMESPACE_NODE :
				value = fNamespaces.remove(((NamespaceNode) toRemove).getPackage());
				break;
			case INode.ZIP_NODE :
				value = fZips.remove(((ZipNode) toRemove).getZipFile());
				break;
			default :
				Assert.isTrue(
					false,
					"Attempt to delete unknown node type from dependency graph");
		}
		return (INode) value;
	}

	/**
	 * For debugging only. Dump the graph in readable form.
	 */
	public void dump(Dumper dumper) {
		dumper.dumpMessage("Namespaces", "");
		dumper.indent();
		for (Enumeration e = fNamespaces.elements(); e.hasMoreElements();) {
			AbstractNode node = (AbstractNode) e.nextElement();
			dumper.dump(node);
		}
		dumper.outdent();

		dumper.dumpMessage("JCUs", "");
		dumper.indent();
		for (Enumeration e = fCompilationUnits.elements(); e.hasMoreElements();) {
			AbstractNode node = (AbstractNode) e.nextElement();
			dumper.dump(node);
		}
		dumper.outdent();

		dumper.dumpMessage("ZIPs", "");
		dumper.indent();
		for (Enumeration e = fZips.elements(); e.hasMoreElements();) {
			AbstractNode node = (AbstractNode) e.nextElement();
			dumper.dump(node);
		}
		dumper.outdent();
	}

	/**
	 * Debugging - Force the order counts.
	 */
	public void forceOrders() {
		for (Enumeration e = fCompilationUnits.elements(); e.hasMoreElements();) {
			AbstractNode node = (AbstractNode) e.nextElement();
			node.getOrder();
		}
	}

	/**
	 * Returns the elements which directly depend on the given element.
	 */
	public Object[] getDependents(Object element) {
		INode node = getNodeFor(element, false);
		if (node == null) {
			return new Object[0];
		}
		INode[] dependentNodes = node.getDependents();
		Object[] dependents = new Object[dependentNodes.length];
		for (int i = 0; i < dependents.length; ++i) {
			dependents[i] = dependentNodes[i].getElement();
		}
		return dependents;
	}

	/**
	 * Returns the number of bytes that the nodes of the dependency graph use.
	 * For debugging and profiling purposes only.
	 */
	public int getFootprint() {
		int size = 0;
		size += getFootprint(fCompilationUnits);
		size += getFootprint(fNamespaces);
		size += getFootprint(fTypes);
		size += getFootprint(fZips);
		return size;
	}

	/**
	 * Returns the number of bytes that the nodes of the dependency graph use.
	 * For debugging and profiling purposes only.
	 */
	public int getFootprint(Dictionary table) {
		int size = 0;
		for (Enumeration e = table.elements(); e.hasMoreElements();) {
			AbstractNode node = (AbstractNode) e.nextElement();
			size += node.getFootprint();
		}
		return size;
	}

	/**
	 * Returns the JCU nodes in the graph.
	 */
	public Enumeration getJCUNodes() {
		return fCompilationUnits.elements();
	}

	/**
	 * Returns the packages this source element is directly dependent on.
	 */
	public IPackage[] getNamespaceDependencies(Object element) {
		INode node = getNodeFor(element, false);
		if (node == null) {
			return new IPackage[0];
		}
		Vector vPackages = new Vector();
		INode[] dependencies = node.getDependencies();
		for (int i = 0; i < dependencies.length; i++) {
			if (dependencies[i].getKind() == INode.NAMESPACE_NODE) {
				vPackages.addElement(((NamespaceNode) dependencies[i]).getPackage());
			}
		}

		IPackage[] results = new IPackage[vPackages.size()];
		vPackages.copyInto(results);
		return results;
	}

	/**
	 * Returns a node in this graph corresponding to the given object.  
	 * A new node is created if necessary.
	 */
	public INode getNodeFor(Object o) {
		return getNodeFor(o, true);
	}

	/**
	 * Returns a node in this graph corresponding to the given object.  
	 * If create is true, the node is added if necessary.
	 */
	public INode getNodeFor(Object o, boolean create) {
		if (o instanceof PackageElement) {
			return getNodeFor((PackageElement) o, create);
		} else
			if (o instanceof IPackage) {
				return getNodeFor((IPackage) o, create);
			} else
				if (o instanceof IPath) {
					return getNodeFor((IPath) o, create);
				} else {
					Assert.isTrue(false, "Unknown kind of node.");
					return null;
				}
	}

	/**
	 * Returns the node for a zip file.  If create is true, the node is
	 * created if necessary.
	 */
	public INode getNodeFor(IPath zipFile, boolean create) {

		/* what about the namespaces for this zip?? */

		INode zipNode = (INode) fZips.get(zipFile);
		if (zipNode == null && create) {
			zipNode = new ZipNode(zipFile);
			fZips.put(zipFile, zipNode);
		}
		return zipNode;
	}

	/**
	 * Returns a node in this graph corresponding to the given node, which
	 * may be from another graph.  A new node is created if necessary.
	 */
	INode getNodeFor(INode node) {
		return getNodeFor(node.getElement(), true);
	}

	/**
	 * Returns the node for the given compilation unit.  The node
	 * is added if necessary.
	 */
	public INode getNodeFor(PackageElement e) {
		return getNodeFor(e, true);
	}

	/**
	 * Returns the node for the given package element. 
	 * If create is true, the node is added if necessary.
	 * A package element may be a JCU, a class file, or a zip file.
	 * Class files are keyed by type handle, and zip files are keyed
	 * by the filename.
	 */
	public INode getNodeFor(PackageElement e, boolean create) {
		INode node;
		if (e.isSource()) {
			/* create a node for this unit if necessary */
			node = (INode) fCompilationUnits.get(e);
			if (node == null && create) {
				node = new JCUNode(e);
				fCompilationUnits.put(e, node);
			}
		} else {
			node = (INode) fTypes.get(e);
			if (node == null && create) {
				node = new TypeNode(e);
				fTypes.put(e, node);
			}
		}
		return node;
	}

	/**
	 * Returns the node for a builder package.  The node is
	 * created if necessary.
	 */
	public INode getNodeFor(IPackage pkg) {
		return getNodeFor(pkg, true);
	}

	/**
	 * Returns the node for a builder package.  If create is true, the node is
	 * created if necessary.
	 */
	public INode getNodeFor(IPackage pkg, boolean create) {

		/* create the namespace if necessary */
		INode pkgNode = (INode) fNamespaces.get(pkg);
		if (pkgNode == null && create) {
			pkgNode = new NamespaceNode(pkg);
			fNamespaces.put(pkg, pkgNode);
		}
		return pkgNode;
	}

	/**
	 * Returns an enumeration over all nodes in the graph.  Uses a composite
	 * enumerator to avoid copying all the elements.
	 */
	public Enumeration getNodes() {
		Vector v =
			new Vector(
				fNamespaces.size() + fTypes.size() + fCompilationUnits.size() + fZips.size());
		for (Enumeration e = fNamespaces.elements(); e.hasMoreElements();) {
			v.addElement(e.nextElement());
		}
		for (Enumeration e = fTypes.elements(); e.hasMoreElements();) {
			v.addElement(e.nextElement());
		}
		for (Enumeration e = fCompilationUnits.elements(); e.hasMoreElements();) {
			v.addElement(e.nextElement());
		}
		for (Enumeration e = fZips.elements(); e.hasMoreElements();) {
			v.addElement(e.nextElement());
		}
		return v.elements();
	}

	/**
	 * Returns the topological order number of the given element.
	 */
	public int getOrder(Object element) {
		AbstractNode node = (AbstractNode) getNodeFor(element, true);
		return node.getOrder();
	}

	/**
	 * Returns the types this source element is directly dependent on.
	 */
	public IType[] getTypeDependencies(Object element) {
		INode node = getNodeFor(element);
		Vector vTypes = new Vector();
		INode[] dependencies = node.getDependencies();
		for (int i = 0; i < dependencies.length; i++) {
			switch (dependencies[i].getKind()) {
				case INode.JCU_NODE :
				case INode.TYPE_NODE :
					IType[] types = dependencies[i].getTypes();
					for (int j = 0; j < types.length; ++j) {
						vTypes.addElement(types[j]);
					}
					break;
			}
		}

		IType[] results = new IType[vTypes.size()];
		vTypes.copyInto(results);
		return results;
	}

	/**
	 * Returns the types that belong to the given source element, or null
	 * if the element is not present.
	 */
	public IType[] getTypes(Object element) {
		// Need to create node if not present to properly handle
		// binary types.
		INode node = getNodeFor(element, true);
		return node == null ? null : node.getTypes();
	}

	/**
	 * Returns a Vector of unused namespace nodes.  
	 * That is, namespace nodes which have no dependents.
	 */
	public Vector getUnusedNamespaceNodes() {
		Vector v = new Vector();
		for (Enumeration e = fNamespaces.elements(); e.hasMoreElements();) {
			INode node = (INode) e.nextElement();
			if (node.getDependents().length == 0) {
				v.addElement(node);
			}
		}
		return v;
	}

	/**
	 * For debugging only -- asserts graph integrity
	 */
	public void integrityCheck() {
		integrityCheck(fCompilationUnits);
		integrityCheck(fTypes);
		integrityCheck(fNamespaces);
	}

	/**
	 * For debugging only -- asserts graph integrity
	 */
	public void integrityCheck(Dictionary table) {
		String msg =
			"Internal Error: the dependency graph is corrupt, do a full build to workaround error.";
		for (Enumeration e = table.elements(); e.hasMoreElements();) {
			AbstractNode node = (AbstractNode) e.nextElement();

			/* do for each dependent of node */
			INode[] nodesThatDependOnMe = node.getDependents();
			for (int i = 0; i < nodesThatDependOnMe.length; i++) {
				/* make sure current node is a dependency of the dependent node */
				INode[] depDeps = nodesThatDependOnMe[i].getDependencies();
				boolean found = false;
				for (int j = depDeps.length; --j >= 0;) {
					if (depDeps[j] == node) {
						found = true;
					}
				}
				Assert.isTrue(found, msg);
			}

			/* do for each dependency of node */
			INode[] nodesThatIDependOn = node.getDependencies();
			for (int i = 0; i < nodesThatIDependOn.length; i++) {
				/* make sure current node is a dependent of the dependency node */
				INode[] depDeps = nodesThatIDependOn[i].getDependents();
				boolean found = false;
				for (int j = depDeps.length; --j >= 0;) {
					if (depDeps[j] == node) {
						found = true;
					}
				}
				Assert.isTrue(found, msg);
			}
		}
	}

	/**
	 * Remove the node for the given element, and mark all dependents
	 * as having to be recompiled, if they haven't been already.
	 * Returns true if the node was successfully removed, and false
	 * if the node could not be found.
	 */
	public boolean remove(Object element) {
		INode node = getNodeFor(element, false);
		if (node == null) {
			return false;
		}

		/* remove dependencies -- this removes backwards links as well */
		node.clearDependencies();

		/* do for each dependent (nodes that depend on me)
		   note: these weren't cleared above */
		INode[] dependents = node.getDependents();
		for (int i = 0; i < dependents.length; i++) {
			INode dep = dependents[i];

			/* remove the dependency */
			dep.removeDependency(node);
			// this must not cause the dependents local to be modified
		}

		return deleteNode(node) != null;
	}

	/**
	 * A package is being removed from the state.  Delete the corresponding namespace nodes,
	 * but only if they have no dependents.
	 */
	public void removePackage(IPackage pkg) {
		INode node = (INode) fNamespaces.get(pkg);
		if (node != null && node.getDependents().length == 0) {
			fNamespaces.remove(pkg);
		}
	}

	/**
	 * Replaces the dependencies and dependents of nodes in the given
	 * table with the corresponding nodes in the new graph.
	 */
	private static void replaceDeps(Dictionary table, DependencyGraph newGraph) {
		for (Enumeration e = table.elements(); e.hasMoreElements();) {
			AbstractNode node = (AbstractNode) e.nextElement();
			node.replaceDeps(newGraph);
		}
	}

	/**
	 * Returns the number of nodes in the graph.
	 */
	public int size() {
		return fNamespaces.size() + fTypes.size() + fCompilationUnits.size();
	}

	public String toString() {
		return "a DependencyGraph";
	}

}
