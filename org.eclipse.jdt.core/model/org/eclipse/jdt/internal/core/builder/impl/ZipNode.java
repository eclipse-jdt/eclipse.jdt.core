package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

/**
 * A node in the dependency graph representing a jar or zip file.  All
 * types and JCUs that depend on a file in the jar have a dependency
 * to the JarNode for that file.
 */
public class ZipNode extends AbstractNode {
	/**
	 * The fully qualified name of the jar file.  Should
	 * this be an IPath or an IFile?
	 */
	IPath fZipFile;
/**
 * Creates a new JarNode instance.
 */
protected ZipNode(IPath zipPath) {
	fZipFile = zipPath;
}
/**
 * Creates and returns a copy of this node.
 */
public AbstractNode copy() {
	return new ZipNode(fZipFile);
}
/**
 * @see INode
 */
public Object getElement() {
	return fZipFile;
}
/**
 * @see INode
 */
public int getKind() {
	return INode.ZIP_NODE;
}
/**
 * Returns the name of the jar associated with this jar node
 */
public IPath getZipFile() {
	return fZipFile;
}
/**
 * Prints a string representation of the node.  This method is for debugging
 * purposes only.
 */
public String toString() {
	return "ZipNode(" + fZipFile + ")";
}
}
