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
package org.eclipse.jdt.core.dom.rewrite;

import java.util.Collections;
import java.util.List;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.jdt.core.internal.dom.rewrite.ListRewriteEvent;
import org.eclipse.jdt.core.internal.dom.rewrite.RewriteEvent;
import org.eclipse.jdt.core.internal.dom.rewrite.RewriteEventStore;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

/**
 * TODO - missing spec
 * @since 3.0
 */
public final class ListRewriter {
	
	private ASTNode fParent;
	private StructuralPropertyDescriptor fChildProperty;
	private NewASTRewrite fRewriter;


	/* package*/ ListRewriter(NewASTRewrite rewriter, ASTNode parent, StructuralPropertyDescriptor childProperty) {
		fRewriter= rewriter;
		fParent= parent;
		fChildProperty= childProperty;
	}
	
	private RewriteEventStore getRewriteStore() {
		return fRewriter.getRewriteEventStore();
	}
	
	private ListRewriteEvent getEvent() {
		return getRewriteStore().getListEvent(fParent, fChildProperty, true);
	}
	
	/**
	 * Marks the given node as removed. The node must be contained in the list.
	 * @param nodeToRemove The node to be marked as removed.
	 * @param editGroup Collects the generated text edits. <code>null</code> can be passed
	 * to not collect any edits.
	 * @throws IllegalArgumentException Thrown when the node to remove is not a member
	 * of the original list.
	 */
	public void remove(ASTNode nodeToRemove, TextEditGroup editGroup) {
		RewriteEvent event= getEvent().removeEntry(nodeToRemove);
		if (editGroup != null) {
			getRewriteStore().setEventEditGroup(event, editGroup);
		}
	}

	/**
	 * Marks the given node as replaced. The node must be contained in the list.
	 * @param nodeToReplace The node to be marked as replaced
	 *	@param replacingNode The replacing node. The replacing node must be a new node. Use placeholder
	 *	nodes to replace with a copied or moved node.
	 * @param editGroup Collects the generated text edits. <code>null</code> can be passed
	 * to not collect any edits.
	 * @throws IllegalArgumentException Thrown when the node to replace is not a member
	 * of the original list or when the replacing node is not a new node.
	 */
	public void replace(ASTNode nodeToReplace, ASTNode replacingNode, TextEditGroup editGroup) {
		RewriteEvent event= getEvent().replaceEntry(nodeToReplace, replacingNode);
		if (editGroup != null) {
			getRewriteStore().setEventEditGroup(event, editGroup);
		}
	}

	/**
	 * Marks the node as inserted in the list after a given existing node. The existing node must be in the list, either as an original or as a new
	 * node already marked as inserted.
	 * @param nodeToInsert The node to insert. The inserted node must be a new node. Use placeholder
	 *	nodes to insert a copied or moved node.
	 *	@param nodeBefore The node to be before the node to insert.
	 * @param editGroup Collects the generated text edits. <code>null</code> can be passed
	 * to not collect any edits.
	 * @throws IllegalArgumentException Thrown when the existing node is not a member
	 * of the list (original or new) or when the inserted node is not a new node.
	 */
	public void insertAfter(ASTNode nodeToInsert, ASTNode nodeBefore, TextEditGroup editGroup) {
		int index= getEvent().getIndex(nodeBefore, ListRewriteEvent.BOTH);
		if (index == -1) {
			throw new IllegalArgumentException("Node does not exist"); //$NON-NLS-1$
		}
		insertAt(nodeToInsert, index + 1, editGroup);
	}
	
	/**
	 * Marks the node as inserted in the list before a given existing node. The existing node must be in the list, either as an original or as a new
	 * node already marked as inserted.
	 * @param nodeToInsert The node to insert. The inserted node must be a new node. Use placeholder
	 *	nodes to insert a copied or moved node.
	 *	@param nodeAfter The node to be after the node to insert.
	 * @param editGroup Collects the generated text edits. <code>null</code> can be passed
	 * to not collect any edits.
	 * @throws IllegalArgumentException Thrown when the existing node is not a member
	 * of the list or when the inserted node is not a new node.
	 */
	public void insertBefore(ASTNode nodeToInsert, ASTNode nodeAfter, TextEditGroup editGroup) {
		int index= getEvent().getIndex(nodeAfter, ListRewriteEvent.BOTH);
		if (index == -1) {
			throw new IllegalArgumentException("Node does not exist"); //$NON-NLS-1$
		}
		insertAt(nodeToInsert, index, editGroup);
	}
	
	/**
	 * Marks the node as inserted in the list as first element.
	 * @param nodeToInsert The node to insert. The inserted node must be a new node. Use placeholder
	 *	nodes to insert a copied or moved node.
	 * @param editGroup Collects the generated text edits. <code>null</code> can be passed
	 * to not collect any edits.
	 * @throws IllegalArgumentException When the inserted node is not a new node.
	 */
	public void insertFirst(ASTNode nodeToInsert, TextEditGroup editGroup) {
		insertAt(nodeToInsert, 0, editGroup);
	}
	
	/**
	 * Marks the node as inserted in the list as last element.
	 * @param nodeToInsert The node to insert. The inserted node must be a new node. Use placeholder
	 *	nodes to insert a copied or moved node.
	 * @param editGroup Collects the generated text edits. <code>null</code> can be passed
	 * to not collect any edits.
	 * @throws IllegalArgumentException When the inserted node is not a new node.
	 */
	public void insertLast(ASTNode nodeToInsert, TextEditGroup editGroup) {
		insertAt(nodeToInsert, -1, editGroup);
	}

	/**
	 * Marks the node as inserted in the list at a given index. The index correspods to a combined list of original and new
	 * nodes: Nodes remarked as removed are still in this list. 
	 * @param nodeToInsert The node to insert. The inserted node must be a new node. Use placeholder
	 *	nodes to insert a copied or moved node.
	 * @param index The insertion index corresponding to a 'combined list' of original and inserted nodes. <code>-1</code>
	 * as index signals to insert as last element.  
	 * @param editGroup Collects the generated text edits. <code>null</code> can be passed
	 * to not collect any edits.
	 * @throws IllegalArgumentException Thrown when the inserted node is not a new node.
	 * @throws IndexOutOfBoundsException Throws when the index is negative and not -1, or if it is larger
	 * than the size of the combined list.
	 */
	public void insertAt(ASTNode nodeToInsert, int index, TextEditGroup editGroup) {
		RewriteEvent event= getEvent().insert(nodeToInsert, index);
		if (isInsertBoundToPreviousByDefault(nodeToInsert)) {
			getRewriteStore().setInsertBoundToPrevious(nodeToInsert);
		}
		if (editGroup != null) {
			getRewriteStore().setEventEditGroup(event, editGroup);
		}
	}
	
	/*
	 * Heuristic to decide if a inserted node is bound to previous or the next sibling. 
	 */
	private boolean isInsertBoundToPreviousByDefault(ASTNode node) {
		return (node instanceof Statement || node instanceof FieldDeclaration);
	}
	
	/**
	 * @return Returns a list of all orginal nodes. The returned list is not modifiable.
	 */
	public List getOriginalList() {
		List list= (List) getEvent().getOriginalValue();
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * @return Returns a list nodes nodes after the rewrite. The returned list is not modifiable.
	 */
	public List getRewrittenList() {
		List list= (List) getEvent().getNewValue();
		return Collections.unmodifiableList(list);
	}
	

	
	
}
