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
 * For describing manipulations to a child list property of an AST node.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @see NewASTRewrite#getListRewrite(ASTNode, ChildListPropertyDescriptor)
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
	 * Removes the given node from its parent's list property in the rewriter.
	 * The node must be contained in the list.
	 * The AST itself is not actually modified in any way; rather, the rewriter
	 * just records a note that this node has been removed from this list.
	 * 
	 * @param node the node being removed
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node is null, or if the node is not
	 * part of this rewriter's AST, or if the described modification is invalid
	 * (not a member of this node's original list)
	 */
	public void remove(ASTNode node, TextEditGroup editGroup) {
		if (node == null) {
			throw new IllegalArgumentException();
		}
		RewriteEvent event= getEvent().removeEntry(node);
		if (editGroup != null) {
			getRewriteStore().setEventEditGroup(event, editGroup);
		}
	}

	/**
	 * Replaces the given node from its parent's list property in the rewriter.
	 * The node must be contained in the list.
	 * The replacement node must either be brand new (not part of the original AST)
	 * or a placeholder node (for example, one created by
	 * {@link #createTargetNode(ASTNode, boolean)}
	 * or {@link #createStringPlaceholder(String, int)}). The AST itself
     * is not actually modified in any way; rather, the rewriter just records
     * a note that this node has been replaced in this list.
	 * 
	 * @param node the node being replaced
	 * @param replacement the replacement node, or <code>null</code> if no
	 * replacement
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node is null, or if the node is not part
	 * of this rewriter's AST, or if the replacement node is not a new node (or
     * placeholder), or if the described modification is otherwise invalid
     * (not a member of this node's original list)
	 */
	public void replace(ASTNode node, ASTNode replacement, TextEditGroup editGroup) {
		if (node == null) { 
			throw new IllegalArgumentException();
		}
		RewriteEvent event= getEvent().replaceEntry(node, replacement);
		if (editGroup != null) {
			getRewriteStore().setEventEditGroup(event, editGroup);
		}
	}

	/**
	 * Inserts the given node into the list after the given element. 
	 * The existing node must be in the list, either as an original or as a new
	 * node that has been inserted.
	 * The inserted node must either be brand new (not part of the original AST)
	 * or a placeholder node (for example, one created by
	 * {@link #createTargetNode(ASTNode, boolean)}
	 * or {@link #createStringPlaceholder(String, int)}). The AST itself
     * is not actually modified in any way; rather, the rewriter just records
     * a note that this node has been inserted into the list.
	 * 
	 * @param node the node to insert
	 * @param element the element after which the given node is to be inserted
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node or element is null, 
	 * or if the node is not part of this rewriter's AST, or if the inserted node
	 * is not a new node (or placeholder), or if <code>element</code> is not a member
	 * of the list (original or new), or if the described modification is
	 * otherwise invalid
	 */
	public void insertAfter(ASTNode node, ASTNode element, TextEditGroup editGroup) {
		if (node == null || element == null) { 
			throw new IllegalArgumentException();
		}
		int index= getEvent().getIndex(element, ListRewriteEvent.BOTH);
		if (index == -1) {
			throw new IllegalArgumentException("Node does not exist"); //$NON-NLS-1$
		}
		insertAt(node, index + 1, editGroup);
	}
	
	/**
	 * Inserts the given node into the list before the given element. 
	 * The existing node must be in the list, either as an original or as a new
	 * node that has been inserted.
	 * The inserted node must either be brand new (not part of the original AST)
	 * or a placeholder node (for example, one created by
	 * {@link #createTargetNode(ASTNode, boolean)}
	 * or {@link #createStringPlaceholder(String, int)}). The AST itself
     * is not actually modified in any way; rather, the rewriter just records
     * a note that this node has been inserted into the list.
	 * 
	 * @param node the node to insert
	 * @param element the element before which the given node is to be inserted
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node or element is null, 
	 * or if the node is not part of this rewriter's AST, or if the inserted node
	 * is not a new node (or placeholder), or if <code>element</code> is not a member
	 * of the list (original or new), or if the described modification is
	 * otherwise invalid
	 */
	public void insertBefore(ASTNode node, ASTNode element, TextEditGroup editGroup) {
		if (node == null || element == null) { 
			throw new IllegalArgumentException();
		}
		int index= getEvent().getIndex(element, ListRewriteEvent.BOTH);
		if (index == -1) {
			throw new IllegalArgumentException("Node does not exist"); //$NON-NLS-1$
		}
		insertAt(node, index, editGroup);
	}
	
	/**
	 * Inserts the given node into the list at the start of the list.
	 * Equivalent to <code>insertAt(node, 0, editGroup)</code>. 
	 * 
	 * @param node the node to insert
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node is null, or if the node is not part
	 * of this rewriter's AST, or if the inserted node is not a new node (or
     * placeholder), or if the described modification is otherwise invalid
     * (not a member of this node's original list)
     * @see #insertAt(ASTNode, int, TextEditGroup)
	 */
	public void insertFirst(ASTNode node, TextEditGroup editGroup) {
		insertAt(node, 0, editGroup);
	}
	
	/**
	 * Inserts the given node into the list at the end of the list.
	 * Equivalent to <code>insertAt(node, -1, editGroup)</code>. 
	 * 
	 * @param node the node to insert
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node is null, or if the node is not part
	 * of this rewriter's AST, or if the inserted node is not a new node (or
     * placeholder), or if the described modification is otherwise invalid
     * (not a member of this node's original list)
     * @see #insertAt(ASTNode, int, TextEditGroup)
	 */
	public void insertLast(ASTNode node, TextEditGroup editGroup) {
		insertAt(node, -1, editGroup);
	}

	/**
	 * Inserts the given node into the list at the given index. 
	 * The index corresponds to a combined list of original and new nodes;
	 * removed or replaced nodes are still in the combined list.
	 * The inserted node must either be brand new (not part of the original AST)
	 * or a placeholder node (for example, one created by
	 * {@link #createTargetNode(ASTNode, boolean)}
	 * or {@link #createStringPlaceholder(String, int)}). The AST itself
     * is not actually modified in any way; rather, the rewriter just records
     * a note that this node has been inserted into the list.
	 * 
	 * @param node the node to insert
	 * @param index insertion index in the combined list of original and
	 * inserted nodes; <code>-1</code> indicates insertion as the last element
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node is null, or if the node is not part
	 * of this rewriter's AST, or if the inserted node is not a new node (or
     * placeholder), or if the described modification is otherwise invalid
     * (not a member of this node's original list)
	 * @throws IndexOutOfBoundsException if the index is negative and not -1, 
	 * or if it is larger than the size of the combined list
	 */
	public void insertAt(ASTNode node, int index, TextEditGroup editGroup) {
		if (node == null) { 
			throw new IllegalArgumentException();
		}
		RewriteEvent event= getEvent().insert(node, index);
		if (isInsertBoundToPreviousByDefault(node)) {
			getRewriteStore().setInsertBoundToPrevious(node);
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
	 * Returns the original nodes in the list property managed by this
	 * rewriter. The returned list is unmodifiable.
	 * 
	 * @return a list of all original nodes in the list
	 */
	public List getOriginalList() {
		List list= (List) getEvent().getOriginalValue();
		return Collections.unmodifiableList(list);
	}
	
	/**
	 * Returns the nodes in the revised list property managed by this
	 * rewriter. The returned list is unmodifiable.
	 * 
	 * @return a list of all nodes in the list taking into account 
	 * all the described changes
	 */
	public List getRewrittenList() {
		List list= (List) getEvent().getNewValue();
		return Collections.unmodifiableList(list);
	}

}
