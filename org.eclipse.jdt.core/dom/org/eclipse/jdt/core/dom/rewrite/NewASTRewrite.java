/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom.rewrite;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.jface.text.IDocument;

import org.eclipse.jdt.core.internal.dom.rewrite.ASTRewriteAnalyzer;
import org.eclipse.jdt.core.internal.dom.rewrite.NodeInfoStore;
import org.eclipse.jdt.core.internal.dom.rewrite.NodeRewriteEvent;
import org.eclipse.jdt.core.internal.dom.rewrite.RewriteEventStore;
import org.eclipse.jdt.core.internal.dom.rewrite.RewriteRuntimeException;
import org.eclipse.jdt.core.internal.dom.rewrite.TrackedNodePosition;
import org.eclipse.jdt.core.internal.dom.rewrite.RewriteEventStore.CopySourceInfo;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

/**
 * Work in progress.
 */
public class NewASTRewrite {
	
	/** Constant used to create place holder nodes */
	public static final int UNKNOWN= NodeInfoStore.UNKNOWN;
	public static final int BLOCK= NodeInfoStore.BLOCK;
	public static final int EXPRESSION= NodeInfoStore.EXPRESSION;
	public static final int STATEMENT= NodeInfoStore.STATEMENT;
	public static final int SINGLEVAR_DECLARATION= NodeInfoStore.SINGLEVAR_DECLARATION;
	public static final int TYPE= NodeInfoStore.TYPE;
	public static final int NAME= NodeInfoStore.NAME;
	public static final int JAVADOC= NodeInfoStore.JAVADOC;
	public static final int VAR_DECLARATION_FRAGMENT= NodeInfoStore.VAR_DECLARATION_FRAGMENT;
	public static final int TYPE_DECLARATION= NodeInfoStore.TYPE_DECLARATION;
	public static final int FIELD_DECLARATION= NodeInfoStore.FIELD_DECLARATION;
	public static final int METHOD_DECLARATION= NodeInfoStore.METHOD_DECLARATION;
	public static final int INITIALIZER= NodeInfoStore.INITIALIZER;
	public static final int PACKAGE_DECLARATION= NodeInfoStore.PACKAGE_DECLARATION;
	public static final int IMPORT_DECLARATION= NodeInfoStore.IMPORT_DECLARATION;
		

	/** root node for the rewrite: Only nodes under this root are accepted */
	private AST fAST;

	protected final RewriteEventStore fEventStore;
	protected final NodeInfoStore fNodeStore;
	
	public NewASTRewrite(AST ast) {
		fAST= ast;
		fEventStore= new RewriteEventStore();
		fNodeStore= new NodeInfoStore(ast);
	}
	
	/**
	 * @return Returns the AST the rewrite was set up on.
	 */
	public AST getAST() {
		return fAST;
	}
			
	protected RewriteEventStore getRewriteEventStore() {
		return fEventStore;
	}
	
	protected NodeInfoStore getNodeStore() {
		return fNodeStore;
	}
	
	/**
	 * Performs the rewrite: The rewrite events are translated to the corresponding in text changes.
	 * @param document Document which describes the code of the AST that is passed in in the
	 * constructor. This document is accessed read-only.
	 * @return Returns the edit describing the text changes.
	 */
	public TextEdit rewriteAST(IDocument document, Map options) throws RewriteException {
		TextEdit result= new MultiTextEdit();
		
		ASTNode rootNode= getRootNode();
		if (rootNode != null) {
			fEventStore.markMovedNodesRemoved();
			try {
				CompilationUnit astRoot= (CompilationUnit) rootNode.getRoot();
				ASTRewriteAnalyzer visitor= new ASTRewriteAnalyzer(document, astRoot, result, fEventStore, fNodeStore, options);
				rootNode.accept(visitor);
			} catch (RewriteRuntimeException e) {
				throw new RewriteException(e.getCause());
			}
		}
		return result;
	}
	
	private ASTNode getRootNode() {
		ASTNode node= null;
		int start= -1;
		int end= -1;
		
		for (Iterator iter= fEventStore.getChangeRootIterator(); iter.hasNext();) {
			ASTNode curr= (ASTNode) iter.next();
			if (!RewriteEventStore.isNewNode(curr)) {
				int currStart= curr.getStartPosition();
				int currEnd= currStart + curr.getLength();
				if (node == null || currStart < start && currEnd > end) {
					start= currStart;
					end= currEnd;
					node= curr;
				} else if (currStart < start) {
					start= currStart;
				} else if (currEnd > end) {
					end= currEnd;
				}
			}
		}
		if (node != null) {
			int currStart= node.getStartPosition();
			int currEnd= currStart + node.getLength();
			while (start < currStart || end > currEnd) { // go up until a node covers all
				node= node.getParent();
				currStart= node.getStartPosition();
				currEnd= currStart + node.getLength();
			}
			ASTNode parent= node.getParent(); // go up until a parent has different range
			while (parent != null && parent.getStartPosition() == node.getStartPosition() && parent.getLength() == node.getLength()) {
				node= parent;
				parent= node.getParent();
			}
		}
		return node;

	}
		
		
	/**
	 * Marks an existing node as removed.
	 * @param node The node to be marked as removed.
	 * @param editGroup Description of the change.
	 */
	public final void markAsRemoved(ASTNode node, TextEditGroup editGroup) {
		StructuralPropertyDescriptor property= node.getLocationInParent();
		if (property.isChildListProperty()) {
			getListRewrite(node.getParent(), (ChildListPropertyDescriptor) property).remove(node, editGroup);
		} else {
			set(node.getParent(), property, null, editGroup);
		}
	}
	
	/**
	 * Marks an existing node as replaced by a new node. The replacing node must be new or
	 * a placeholder.
	 * @param node The node to be marked as replaced.
	 * @param replacingNode The node replacing the node.
	 * @param editGroup Description of the change. 
	 */		
	public final void markAsReplaced(ASTNode node, ASTNode replacingNode, TextEditGroup editGroup) {
		StructuralPropertyDescriptor property= node.getLocationInParent();
		if (property.isChildListProperty()) {
			getListRewrite(node.getParent(), (ChildListPropertyDescriptor) property).replace(node, replacingNode, editGroup);
		} else {
			set(node.getParent(), property, replacingNode, editGroup);
		}
	}

	/**
	 * Describes a change of a property  The replacing node must be new or a placeholder or can be
	 * <code>null</code> to remove the property.
	 * @param parent The node's parent node.
	 * @param childProperty The node's child property in the parent. 
	 * @param replacingNode The node that replaces the original node or <code>null</code>
	 * @param editGroup Collects the generated text edits or <code>null</code> if
	 * no edits should be collected.
	 * @throws IllegalArgumentException An <code>IllegalArgumentException</code> is either the parent node is
	 * not inside the rewriters parent, the property is not a node property or the described modification is not
	 * valid (e.g. when removing a required node).
	 */
	public final void set(ASTNode parent, StructuralPropertyDescriptor childProperty, Object replacingNode, TextEditGroup editGroup) {
		validateIsInsideAST(parent);
		NodeRewriteEvent nodeEvent= fEventStore.getNodeEvent(parent, childProperty, true);
		if (replacingNode == null) {
			validatePropertyType(childProperty, replacingNode);
		}
		nodeEvent.setNewValue(replacingNode);
		if (editGroup != null) {
			fEventStore.setEventEditGroup(nodeEvent, editGroup);
		}
	}

	/**
	 * Gets a rewriter to modify the given list.
	 * @param parent The parent node.
	 * @param childProperty The child property
	 * @return
	 */
	public ListRewriter getListRewrite(ASTNode parent, ChildListPropertyDescriptor childProperty) {
		validateIsInsideAST(parent);
		validateIsListProperty(childProperty);
		
		return new ListRewriter(this, parent, childProperty);
	}
		
	/**
	 * Marks a node as tracked. The edits added to the group editGroup can be used to get the
	 * position of the node after the rewrite operation.
	 * @param node The node to track
	 */
	public final ITrackedNodePosition markAsTracked(ASTNode node) {
		if (fEventStore.getTrackedNodeData(node) != null) {
			throw new IllegalArgumentException("Node is already marked as tracked"); //$NON-NLS-1$
		}
		TextEditGroup editGroup= new TextEditGroup("internal"); //$NON-NLS-1$
		fEventStore.setTrackedNodeData(node, editGroup);
		return new TrackedNodePosition(editGroup, node);
	}	
			
	protected final void validateIsInsideAST(ASTNode node) {
		if (node.getStartPosition() == -1) {
			throw new IllegalArgumentException("Node is not an existing node"); //$NON-NLS-1$
		}
	
		if (node.getAST() != getAST()) {
			throw new IllegalArgumentException("Node is not inside the AST"); //$NON-NLS-1$
		}
	}
	
	protected void validateIsListProperty(StructuralPropertyDescriptor property) {
		if (!property.isChildListProperty()) {
			String message= property.getId() + " is not a list property"; //$NON-NLS-1$
			throw new IllegalArgumentException(message);
		}
	}
	
	private void validatePropertyType(StructuralPropertyDescriptor prop, Object node) {
		if (prop.isChildListProperty()) {
			String message= "Can not modify a list property, use a list rewriter"; //$NON-NLS-1$
			throw new IllegalArgumentException(message);		
		}
//		if (node == null) {
//			if (prop.isSimpleProperty() || (prop.isChildProperty() && ((ChildPropertyDescriptor) prop).isMandatory())) {
//				String message= "Can not remove property " + prop.getId(); //$NON-NLS-1$
//				throw new IllegalArgumentException(message);
//			}
//		} else {
//			if (!prop.getNodeClass().isInstance(node)) {
//				String message= node.getClass().getName() +  " is not a valid type for property " + prop.getId(); //$NON-NLS-1$
//				throw new IllegalArgumentException(message);
//			}
//		}
	}
	
	/**
	 * Returns the node type that should be used to create a place holder for the given node
	 * <code>existingNode</code>.
	 * 
	 * @param existingNode an existing node for which a place holder is to be created
	 * @return the node type of a potential place holder
	 */
	public static int getPlaceholderType(ASTNode existingNode) {
		return NodeInfoStore.getPlaceholderType(existingNode);
	}
			
	/**
	 * Creates a target node for a source string to be inserted without being formatted. A target node can
	 * be inserted or used to replace at the target position.
	 * @param code String that will be inserted. The string must not have extra indent.
	 * @param nodeType the type of the place holder. Valid values are <code>METHOD_DECLARATION</code>,
	 * <code>FIELD_DECLARATION</code>, <code>INITIALIZER</code>,
	 * <code>TYPE_DECLARATION</code>, <code>BLOCK</code>, <code>STATEMENT</code>,
	 *  <code>SINGLEVAR_DECLARATION</code>,<code> VAR_DECLARATION_FRAGMENT</code>,
	 * <code>TYPE</code>, <code>EXPRESSION</code>, <code>NAME</code>
	 * <code>PACKAGE_DECLARATION</code>, <code>IMPORT_DECLARATION</code> and <code>JAVADOC</code>.
	 * @return Returns the place holder node
	 */
	public final ASTNode createStringPlaceholder(String code, int nodeType) {
		ASTNode placeholder= fNodeStore.newPlaceholderNode(nodeType);
		if (placeholder == null) {
			throw new IllegalArgumentException("String placeholder is not supported for type" + nodeType); //$NON-NLS-1$
		}
		
		fNodeStore.markAsStringPlaceholder(placeholder, code);
		return placeholder;
	}
	
	public ASTNode createTargetNode(ASTNode node, boolean isMove) {
		validateIsInsideAST(node);
		CopySourceInfo info= fEventStore.markAsCopySource(node.getParent(), node.getLocationInParent(), node, isMove);
	
		ASTNode placeholder= fNodeStore.newPlaceholderNode(getPlaceholderType(node));
		if (placeholder == null) {
			throw new IllegalArgumentException("Creating a target node is not supported for nodes of type" + node.getClass().getName()); //$NON-NLS-1$
		}
		fNodeStore.markAsCopyTarget(placeholder, info);
		
		return placeholder;		
	}

	/**
	 * Creates a target node for a node to be copied. A target node can be inserted or used
	 * to replace at the target position.
	 * @param node The node to create a copy placeholder for.
	 * @return The placeholder to be used at the copy destination.
	 */
	public final ASTNode createCopyTarget(ASTNode node) {
		return createTargetNode(node, false);
	}
	
	/**
	 * Creates a target node for a node to be moved. A target node can be inserted or used
	 * to replace at the target position. The source node has to be marked as removed or replaced.
	 * @param node The node to create a move placeholder for.
	 * @return The placeholder to be used at the move destination.
	 */
	public final ASTNode createMoveTarget(ASTNode node) {
		return createTargetNode(node, true);
	}	
			
	public String toString() {
		StringBuffer buf= new StringBuffer();
		buf.append("Events:\n"); //$NON-NLS-1$
		buf.append(fEventStore.toString());
		return buf.toString();
	}
	

}
