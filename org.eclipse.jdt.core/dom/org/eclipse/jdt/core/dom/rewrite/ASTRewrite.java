/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteAnalyzer;
import org.eclipse.jdt.internal.core.dom.rewrite.NodeInfoStore;
import org.eclipse.jdt.internal.core.dom.rewrite.NodeRewriteEvent;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;
import org.eclipse.jdt.internal.core.dom.rewrite.TrackedNodePosition;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore.CopySourceInfo;

/**
 * Infrastucture for modifying code by describing changes to AST nodes.
 * The AST rewriter collects descriptions of modifications to nodes and
 * translates these descriptions into text edits that can then be applied to
 * the original source. The key thing is that this is all done without actually
 * modifying the original AST, which has the virtue of allowing one to entertain
 * several alternate sets of changes on the same AST (e.g., for calculating
 * quick fix proposals). The rewrite infrastructure tries to generate minimal
 * text changes, preserve existing comments and indentation, and follow code
 * formatter settings. If the freedom to explore multiple alternate changes is
 * not required, consider using the AST's built-in rewriter 
 * (see {@link org.eclipse.jdt.core.dom.CompilationUnit#rewrite(IDocument, Map)}).
 * <p>
 * The following code snippet illustrated usage of this class:
 * </p>
 * <pre>
 * Document doc = new Document("import java.util.List;\nclass X {}\n");
 * ASTParser parser = ASTParser.newParser(AST.JLS3);
 * parser.setSource(doc.get().toCharArray());
 * CompilationUnit cu = (CompilationUnit) parser.createAST(null);
 * AST ast = cu.getAST();
 * ImportDeclaration id = ast.newImportDeclaration();
 * id.setName(ast.newName(new String[] {"java", "util", "Set"});
 * ASTRewrite rewriter = ASTRewrite.create(ast);
 * TypeDeclaration td = (TypeDeclaration) cu.types().get(0);
 * ITrackedNodePosition tdLocation = rewriter.track(td);
 * ListRewriter lrw = rewriter.getListRewrite(cu,
 *                       CompilationUnit.IMPORTS_PROPERTY);
 * lrw.insertLast(id, null);
 * TextEdit edits = rewriter.rewriteAST(document, null);
 * UndoEdit undo = edits.apply(document);
 * assert "import java.util.List;\nimport java.util.Set;\nclass X {}"
 *   .equals(doc.get().toCharArray());
 * // tdLocation.getStartPosition() and tdLocation.getLength()
 * // are new source range for "class X {}" in doc.get()
 * </pre>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @since 3.0
 */
public class ASTRewrite {

	/** root node for the rewrite: Only nodes under this root are accepted */
	private final AST ast;

	private final RewriteEventStore eventStore;
	private final NodeInfoStore nodeStore;
	
	/**
	 * Target source range computer; null means uninitialized;
	 * lazy initialized to <code>new TargetSourceRangeComputer()</code>.
	 * @since 3.1
	 */
	private TargetSourceRangeComputer targetSourceRangeComputer = null;
	
	/**
	 * Creates a new instance for describing manipulations of
	 * the given AST.
	 * 
	 * @param ast the AST whose nodes will be rewritten
	 * @return the new rewriter instance
	 */
	public static ASTRewrite create(AST ast) {
		return new ASTRewrite(ast);
	}

	/**
	 * Internal constructor. Creates a new instance for the given AST.
	 * Clients should use {@link #create(AST)} to create instances.
	 * 
	 * @param ast the AST being rewritten
	 */
	protected ASTRewrite(AST ast) {
		this.ast= ast;
		this.eventStore= new RewriteEventStore();
		this.nodeStore= new NodeInfoStore(ast);
	}
	
	/**
	 * Returns the AST the rewrite was set up on.
	 * 
	 * @return the AST the rewrite was set up on
	 */
	public final AST getAST() {
		return this.ast;
	}
			
	/**
	 * Internal method. Returns the internal event store.
	 * Clients should not use.
	 * @return Returns the internal event store. Clients should not use.
	 */
	protected final RewriteEventStore getRewriteEventStore() {
		return this.eventStore;
	}
	
	/**
	 * Internal method. Returns the internal node info store.
	 * Clients should not use.
	 * @return Returns the internal info store. Clients should not use.
	 */
	protected final NodeInfoStore getNodeStore() {
		return this.nodeStore;
	}
	
	/**
	 * Converts all modifications recorded by this rewriter
	 * into an object representing the corresponding text
	 * edits to the given document containing the original source
	 * code. The document itself is not modified.
	 * <p>
	 * For nodes in the original that are being replaced or deleted,
	 * this rewriter computes the adjusted source ranges
	 * by calling <code>getTargetSourceRangeComputer().computeSourceRange(node)</code>.
	 * </p>
	 * <p>
	 * Calling this methods does not discard the modifications
	 * on record. Subsequence modifications are added to the ones
	 * already on record. If this method is called again later,
	 * the resulting text edit object will accurately reflect
	 * the net cumulative affect of all those changes.
	 * </p>
	 * 
	 * @param document original document containing source code
	 * @param options the table of formatter options
	 * (key type: <code>String</code>; value type: <code>String</code>);
	 * or <code>null</code> to use the standard global options
	 * {@link JavaCore#getOptions() JavaCore.getOptions()}
	 * @return text edit object describing the changes to the
	 * document corresponding to the changes recorded by this rewriter
	 * @throws IllegalArgumentException An <code>IllegalArgumentException</code>
	 * is thrown if the document passed does not correspond to the AST that is rewritten. 
	 */
	public TextEdit rewriteAST(IDocument document, Map options) throws IllegalArgumentException {
		if (document == null) {
			throw new IllegalArgumentException();
		}
		TextEdit result= new MultiTextEdit();
		
		ASTNode rootNode= getRootNode();
		if (rootNode != null) {
			//validateASTNotModified(rootNode);
			
			getRewriteEventStore().markMovedNodesRemoved();

			ASTRewriteAnalyzer visitor= new ASTRewriteAnalyzer(document, result, this.eventStore, this.nodeStore, options, getExtendedSourceRangeComputer());
			rootNode.accept(visitor); // throws IllegalArgumentException
		}
		return result;
	}
	
	private ASTNode getRootNode() {
		ASTNode node= null;
		int start= -1;
		int end= -1;
		
		for (Iterator iter= getRewriteEventStore().getChangeRootIterator(); iter.hasNext();) {
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
	
	/*
	private void validateASTNotModified(ASTNode root) throws IllegalArgumentException {
		GenericVisitor isModifiedVisitor= new GenericVisitor() {
			protected boolean visitNode(ASTNode node) {
				if ((node.getFlags() & ASTNode.ORIGINAL) == 0) {
					throw new IllegalArgumentException("The AST that is rewritten must not be modified."); //$NON-NLS-1$
				}
				return true;
			}
		};
		root.accept(isModifiedVisitor);
	}
	*/
		
	/**
	 * Removes the given node from its parent in this rewriter. The AST itself
     * is not actually modified in any way; rather, the rewriter just records
     * a note that this node should not be there.
	 * 
	 * @param node the node being removed
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node is null, or if the node is not
	 * part of this rewriter's AST, or if the described modification is invalid
	 * (such as removing a required node)
	 */
	public final void remove(ASTNode node, TextEditGroup editGroup) {
		if (node == null) {
			throw new IllegalArgumentException();
		}
		StructuralPropertyDescriptor property= node.getLocationInParent();
		if (property.isChildListProperty()) {
			getListRewrite(node.getParent(), (ChildListPropertyDescriptor) property).remove(node, editGroup);
		} else {
			set(node.getParent(), property, null, editGroup);
		}
	}
	
	/**
	 * Replaces the given node in this rewriter. The replacement node
	 * must either be brand new (not part of the original AST) or a placeholder
	 * node (for example, one created by {@link #createCopyTarget(ASTNode)}
	 * or {@link #createStringPlaceholder(String, int)}). The AST itself
     * is not actually modified in any way; rather, the rewriter just records
     * a note that this node has been replaced.
	 * 
	 * @param node the node being replaced
	 * @param replacement the replacement node, or <code>null</code> if no
	 * replacement
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node is null, or if the node is not part
	 * of this rewriter's AST, or if the replacement node is not a new node (or
     * placeholder), or if the described modification is otherwise invalid
	 */		
	public final void replace(ASTNode node, ASTNode replacement, TextEditGroup editGroup) {
		if (node == null) {
			throw new IllegalArgumentException();
		}
		StructuralPropertyDescriptor property= node.getLocationInParent();
		if (property.isChildListProperty()) {
			getListRewrite(node.getParent(), (ChildListPropertyDescriptor) property).replace(node, replacement, editGroup);
		} else {
			set(node.getParent(), property, replacement, editGroup);
		}
	}

	/**
	 * Sets the given property of the given node. If the given property is a child
     * property, the value must be a replacement node that is either be brand new
     * (not part of the original AST) or a placeholder node (for example, one
     * created by {@link #createCopyTarget(ASTNode)}
	 * or {@link #createStringPlaceholder(String, int)}); or it must be
	 * </code>null</code>, indicating that the child should be deleted.
	 * If the given property is a simple property, the value must be the new
	 * value (primitive types must be boxed) or </code>null</code>.
     * The AST itself is not actually modified in any way; rather, the rewriter
     * just records a note that this node has been changed in the specified way.
	 * 
	 * @param node the node
	 * @param property the node's property; either a simple property or a child property
	 * @param value the replacement child or new value, or <code>null</code> if none
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node or property is null, or if the node
	 * is not part of this rewriter's AST, or if the property is not a node property,
	 * or if the described modification is invalid
	 */
	public final void set(ASTNode node, StructuralPropertyDescriptor property, Object value, TextEditGroup editGroup) {
		if (node == null || property == null) {
			throw new IllegalArgumentException();
		}
		validateIsInsideAST(node);
		validatePropertyType(property, value);

		NodeRewriteEvent nodeEvent= this.eventStore.getNodeEvent(node, property, true);
		nodeEvent.setNewValue(value);
		if (editGroup != null) {
			this.eventStore.setEventEditGroup(nodeEvent, editGroup);
		}
	}

	/**
	 * Creates and returns a new rewriter for describing modifications to the
	 * given list property of the given node.
	 * 
	 * @param node the node
	 * @param property the node's property; the child list property
	 * @return a new list rewriter object
	 * @throws IllegalArgumentException if the node or property is null, or if the node
	 * is not part of this rewriter's AST, or if the property is not a node property,
	 * or if the described modification is invalid
	 */
	public final ListRewrite getListRewrite(ASTNode node, ChildListPropertyDescriptor property) {
		if (node == null || property == null) {
			throw new IllegalArgumentException();
		}
		validateIsListProperty(property);
		
		return new ListRewrite(this, node, property);
	}
		
	/**
	 * Returns an object that tracks the source range of the given node
	 * across the rewrite to its AST. Upon return, the result object reflects
	 * the given node's current source range in the AST. After
	 * <code>rewrite</code> is called, the result object is updated to
	 * reflect the given node's source range in the rewritten AST.
	 * 
	 * @param node the node to track
	 * @return an object that tracks the source range of <code>node</code>
	 * @throws IllegalArgumentException if the node is null, or if the node
	 * is not part of this rewriter's AST, or if the node is already being
	 * tracked
	 */
	public final ITrackedNodePosition track(ASTNode node) {
		if (node == null) {
			throw new IllegalArgumentException();
		}
		TextEditGroup group= this.eventStore.getTrackedNodeData(node);
		if (group == null) {
			group= new TextEditGroup("internal"); //$NON-NLS-1$
			this.eventStore.setTrackedNodeData(node, group);
		}
		return new TrackedNodePosition(group, node);
	}	
			
	private void validateIsInsideAST(ASTNode node) {
		if (node.getStartPosition() == -1) {
			throw new IllegalArgumentException("Node is not an existing node"); //$NON-NLS-1$
		}
	
		if (node.getAST() != getAST()) {
			throw new IllegalArgumentException("Node is not inside the AST"); //$NON-NLS-1$
		}
	}
	
	private void validateIsListProperty(StructuralPropertyDescriptor property) {
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
	 * Creates and returns a placeholder node for a source string that is to be inserted into
	 * the output document at the position corresponding to the placeholder.
	 * The string will be inserted without being reformatted beyond correcting
	 * the indentation level. The placeholder node can either be inserted as new or
	 * used to replace an existing node.
	 * 
	 * @param code the string to be inserted; lines should should not have extra indentation
	 * @param nodeType the ASTNode type that corresponds to the passed code. 
	 * @return the new placeholder node
	 * @throws IllegalArgumentException if the code is null, or if the node
	 * type is invalid
	 */
	public final ASTNode createStringPlaceholder(String code, int nodeType) {
		if (code == null) {
			throw new IllegalArgumentException();
		}
		ASTNode placeholder= getNodeStore().newPlaceholderNode(nodeType);
		if (placeholder == null) {
			throw new IllegalArgumentException("String placeholder is not supported for type" + nodeType); //$NON-NLS-1$
		}
		
		getNodeStore().markAsStringPlaceholder(placeholder, code);
		return placeholder;
	}
	
	/**
	 * Creates and returns a node that represents a sequence of nodes. 
	 * Each of the given nodes must be either be brand new (not part of the original AST), or
	 * a placeholder node (for example, one created by {@link #createCopyTarget(ASTNode)}
	 * or {@link #createStringPlaceholder(String, int)}), or another group node.
	 * The type of the returned node is unspecified. The returned node can be used
	 * to replace an existing node (or as an element of another group node).
	 * When the document is rewritten, the source code for each of the given nodes is
	 * inserted, in order, into the output document at the position corresponding to the
	 * group (indentation is adjusted).
	 * 
	 * @param targetNodes the nodes to go in the group
	 * @return the new group node
	 * @throws IllegalArgumentException if the targetNodes is <code>null</code> or empty
	 * @since 3.1
	 */
	public final ASTNode createGroupNode(ASTNode[] targetNodes) {
		if (targetNodes == null || targetNodes.length == 0) {
			throw new IllegalArgumentException();
		}
		Block res= getNodeStore().createCollapsePlaceholder();
		ListRewrite listRewrite= getListRewrite(res, Block.STATEMENTS_PROPERTY);
		for (int i= 0; i < targetNodes.length; i++) {
			listRewrite.insertLast(targetNodes[i], null);
		}
		return res;
	}
	
	
	private ASTNode createTargetNode(ASTNode node, boolean isMove) {
		if (node == null) {
			throw new IllegalArgumentException();
		}
		validateIsInsideAST(node);
		CopySourceInfo info= getRewriteEventStore().markAsCopySource(node.getParent(), node.getLocationInParent(), node, isMove);
	
		ASTNode placeholder= getNodeStore().newPlaceholderNode(node.getNodeType());
		if (placeholder == null) {
			throw new IllegalArgumentException("Creating a target node is not supported for nodes of type" + node.getClass().getName()); //$NON-NLS-1$
		}
		getNodeStore().markAsCopyTarget(placeholder, info);
		
		return placeholder;		
	}

	/**
	 * Creates and returns a placeholder node for a true copy of the given node.
	 * The placeholder node can either be inserted as new or used to replace an
	 * existing node. When the document is rewritten, a copy of the source code 
	 * for the given node is inserted into the output document at the position
	 * corresponding to the placeholder (indentation is adjusted).
	 * 
	 * @param node the node to create a copy placeholder for
	 * @return the new placeholder node
	 * @throws IllegalArgumentException if the node is null, or if the node
	 * is not part of this rewriter's AST
	 */
	public final ASTNode createCopyTarget(ASTNode node) {
		return createTargetNode(node, false);
	}
	
	/**
	 * Creates and returns a placeholder node for the new locations of the given node.
	 * After obtaining a placeholder, the node should then to be removed or replaced.
	 * The placeholder node can either be inserted as new or used to replace an
	 * existing node. When the document is rewritten, the source code for the given
	 * node is inserted into the output document at the position corresponding to the
	 * placeholder (indentation is adjusted). 
	 * 
	 * @param node the node to create a move placeholder for
	 * @return the new placeholder node
	 * @throws IllegalArgumentException if the node is null, or if the node
	 * is not part of this rewriter's AST
	 */
	public final ASTNode createMoveTarget(ASTNode node) {
		return createTargetNode(node, true);
	}	

	/**
	 * Returns the extended source range computer for this AST rewriter.
	 * The default value is a <code>new ExtendedSourceRangeComputer()</code>.
	 * 
	 * @return an extended source range computer
	 * @since 3.1
	 */
	public final TargetSourceRangeComputer getExtendedSourceRangeComputer() {
		if (this.targetSourceRangeComputer == null) {
			// lazy initialize
			this.targetSourceRangeComputer = new TargetSourceRangeComputer(); 
		}
		return this.targetSourceRangeComputer;
	}
	
	/**
	 * Sets a custom target source range computer for this AST rewriter. This is advanced feature to modify how
	 * comments are assotiated with nodes, which should be done only in special cases.
	 * 
	 * @param computer a target source range computer,
	 * or <code>null</code> to restore the default value of
	 * <code>new TargetSourceRangeComputer()</code>
	 * @since 3.1
	 */
	public final void setTargetSourceRangeComputer(TargetSourceRangeComputer computer) {
		// if computer==null, rely on lazy init code in getTargetSourceRangeComputer()
		this.targetSourceRangeComputer = computer;
	}
	
	/**
	 * Returns a string suitable for debugging purposes (only).
	 * 
	 * @return a debug string
	 */
	public String toString() {
		StringBuffer buf= new StringBuffer();
		buf.append("Events:\n"); //$NON-NLS-1$
		// be extra careful of uninitialized or mangled instances
		if (this.eventStore != null) {
			buf.append(this.eventStore.toString());
		}
		return buf.toString();
	}
}
