/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

/**
 * A node event handler is an internal mechanism for receiving
 * notification of changes to nodes in an AST.
 * <p>
 * The default implementation serves as the default event handler
 * that does nothing. Internal subclasses do all the real work.
 * </p>
 * 
 * @see AST#getNodeEventHandler()
 */
class NodeEventHandler {

	/**
	 * Creates a node event handler.
	 */
	NodeEventHandler() {
		// default implementation: do nothing
	}
	
	/**
	 * Reports that the given node is about to lose a child.
	 * The default implementation does nothing.
	 * 
	 * @param node the node about to be modified
	 * @param child the node about to be removed
	 * @param property the child or child list property descriptor
	 * @since 3.0
	 */
	void preRemoveChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
		// do nothing
		// System.out.println("DEL " + property); //$NON-NLS-1$
	}
	
	/**
	 * Reports that the given node is about to have a child replaced.
	 * The first half of an event pair.
	 * The default implementation does nothing.
	 * 
	 * @param node the node about to be modified
	 * @param child the node about to be replaced
	 * @param newChild the replacement child
	 * @param property the child or child list property descriptor
	 * @see #preReplaceChildEvent(ASTNode, ASTNode, ASTNode, StructuralPropertyDescriptor)
	 * @since 3.0
	 */
	void preReplaceChildEvent(ASTNode node, ASTNode child, ASTNode newChild, StructuralPropertyDescriptor property) {
		// do nothing
		// System.out.println("RP1 " + property); //$NON-NLS-1$
	}
	
	/**
	 * Reports that the given node has had its child replaced. The second half
	 * of an event pair. The default implementation does nothing.
	 * 
	 * @param node the node that was modified
	 * @param child the node that was replaced
	 * @param newChild the replacement child
	 * @param property the child or child list property descriptor
	 * @see #postReplaceChildEvent(ASTNode, ASTNode, ASTNode, StructuralPropertyDescriptor)
	 * @since 3.0
	 */
	void postReplaceChildEvent(ASTNode node, ASTNode child, ASTNode newChild, StructuralPropertyDescriptor property) {
		// do nothing
		// System.out.println("RP2 " + property); //$NON-NLS-1$
	}
	
	/**
	 * Reports that the given node has just gained a child.
	 * The default implementation does nothing.
	 * 
	 * @param node the node that was modified
	 * @param child the node that was added as a child
	 * @param property the child or child list property descriptor
	 * @since 3.0
	 */
	void postAddChildEvent(ASTNode node, ASTNode child, StructuralPropertyDescriptor property) {
		// do nothing
		// System.out.println("ADD " + property); //$NON-NLS-1$
	}
	
	/**
	 * Reports that the given node has just changed the value of a
	 * non-child property. The default implementation does nothing.
	 * 
	 * @param node the node that was modified
	 * @param property the property descriptor
	 * @since 3.0
	 */
	void postValueChangeEvent(ASTNode node, SimplePropertyDescriptor property) {
		// do nothing
		// System.out.println("MOD " + property); //$NON-NLS-1$
	}
}
