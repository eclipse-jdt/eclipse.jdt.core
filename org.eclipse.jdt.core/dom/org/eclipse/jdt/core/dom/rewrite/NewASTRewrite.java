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
 * TODO - Work in progress. missing spec
 * <p>
 * <pre>
 * Document doc = new Document("import java.util.List;\nclass X {}\n");
 * ASTParser parser = ASTParser.newParser(AST.LEVEL_3_0);
 * parser.setSource(doc.get().toCharArray());
 * CompilationUnit cu = (CompilationUnit) parser.createAST(null);
 * cu.recordModifications();
 * AST ast = cu.getAST();
 * ImportDeclaration id = ast.newImportDeclaration();
 * id.setName(ast.newName(new String[] {"java", "util", "Set"});
 * NewASTRewrite rewriter = new NewASTRewrite(ast);
 * TypeDeclaration td = (TypeDeclaration) cu.types().get(0);
 * ITrackedNodePosition tdLocation = rewriter.track(td);
 * ListRewriter lrw = rewriter.getListRewrite(cu,
 *                       CompilationUnit.IMPORTS_PROPERTY);
 * lrw.insertLast(id, null);
 * TextEdit edits = rewriter.rewrite(document, null);
 * UndoEdit undo = edits.apply(document);
 * assert "import java.util.List;\nimport java.util.Set;\nclass X {}"
 *   .equals(doc.get().toCharArray());
 * // tdLocation.getStartPosition() and tdLocation.getLength()
 * // are new source range for "class X {}" in doc.get()
 * </pre>
 * </p>
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @since 3.0
 * TODO (david) - name should be "ASTRewrite" (or "ASTRewriter") since that name is available
 * TODO (david) - class should be declared as final to prevent clients from subclassing
 */
public class NewASTRewrite {
	
   /* TODO (david) - For creating placeholders it should be sufficient to have
     * clients supply a concrete ASTNode node type to createStringPlaceholder.
     * If they want to insert a name, they are free to specify either
     * ASTNode.SIMPLE_NAME or ASTNode.QUALIFIED_NAME;
     * either would do equally well. As long as the rewriter can tell that it's a 
     * placeholder, it should be able to deal with it when the time comes.
     * This would allow you to delete all these API constants.
     */			
	/** Constant used to create place holder nodes*/
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

	/* TODO (david) - API fields should not be declared protected; even when clients
	 * can subclass, it is better to provide protected methods that control access to
	 * the field 
	 */
	protected final RewriteEventStore fEventStore;
	protected final NodeInfoStore fNodeStore;
	
	/* TODO (david) - You get more flexibility to evolve an API class
	 * by declaring a public static factory method as API and keeping
	 * the constructor private or package-private.
	 */
	/**
	 * Creates a new AST rewrite for the given AST.
	 * 
	 * @param ast the AST being rewritten
	 */
	public NewASTRewrite(AST ast) {
		fAST= ast;
		fEventStore= new RewriteEventStore();
		fNodeStore= new NodeInfoStore(ast);
	}
	
	/**
	 * Returns the AST the rewrite was set up on.
	 * 
	 * @return the AST the rewrite was set up on
	 * TODO (david) - method should be final
	 */
	public AST getAST() {
		return fAST;
	}
			
	/* TODO (david) - protected methods on API classes are considered API.
	 * These 2 methods returns internal classes, so they should not be API.
	 * Methods should package-private or private.
	 */
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
		
	
	/* TODO (david) - This method would be better called simply "remove"
     * the way it is on ListRewriter.
     */
	/**
	 * Removes the given node from its parent in this rewriter. The AST itself
     * is not actually modified in any way; rather, the rewriter just records
     * a note that this node should not be there.
	 * 
	 * @param node the node being removed
	 * @param editGroup the edit group in which to collect the corresponding
	 * text edits, or <code>null</code> if ungrouped
	 * @throws IllegalArgumentException if the node is null, or if the nodeis not
	 * part of this rewriter's AST, or if the described modification is invalid
	 * (such as removing a required node)
	 */
	public final void markAsRemoved(ASTNode node, TextEditGroup editGroup) {
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
	
	/* TODO (david) - This method would be better called simply "replace"
     * the way it is on ListRewriter.
     */
	/**
	 * Replaces the given node in this rewriter. The replacement node
	 * must either be brand new (not part of the original AST) or a placeholder
	 * node (for example, one created by {@link #createTargetNode(ASTNode, boolean)}
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
	public final void markAsReplaced(ASTNode node, ASTNode replacement, TextEditGroup editGroup) {
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
     * created by {@link #createTargetNode(ASTNode, boolean)}
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
		NodeRewriteEvent nodeEvent= fEventStore.getNodeEvent(node, property, true);
		if (value == null) {
			validatePropertyType(property, value);
		}
		nodeEvent.setNewValue(value);
		if (editGroup != null) {
			fEventStore.setEventEditGroup(nodeEvent, editGroup);
		}
	}

	/* TODO (david) - Methods called getXXX are generally considered accessors 
	 * and are not expected to be creating new instances each time they are called.
	 * A better name for this would be createListRewrite(...).
	 */
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
	public ListRewriter getListRewrite(ASTNode node, ChildListPropertyDescriptor property) {
		if (node == null || property == null) {
			throw new IllegalArgumentException();
		}
		validateIsInsideAST(node);
		validateIsListProperty(property);
		
		return new ListRewriter(this, node, property);
	}
		
	/* TODO (david) - A better name for this method might be track(ASTNode).
	 */
	/* TODO (david) - It seems unnecesssary to prevent a client from tracking
	 * a node more than once. There is no ambiguity in such a situation, and the
	 * implementation is free to return either a new or existing ITrackedNodePosition.
	 */
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
	public final ITrackedNodePosition markAsTracked(ASTNode node) {
		if (node == null) {
			throw new IllegalArgumentException();
		}
		if (fEventStore.getTrackedNodeData(node) != null) {
			throw new IllegalArgumentException("Node is already marked as tracked"); //$NON-NLS-1$
		}
		TextEditGroup editGroup= new TextEditGroup("internal"); //$NON-NLS-1$
		fEventStore.setTrackedNodeData(node, editGroup);
		return new TrackedNodePosition(editGroup, node);
	}	
			
	/* TODO (david) - protected methods on API classes are considered API.
	 * These 2 methods should probably be package-private or private.
	 */
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
	
	/* TODO (david) - Methods called getXXX are generally considered accessors 
	 * and are not expected to be creating new instances each time they are called.
	 * A better name for this would be createPlaceholder(ASTNode).
	 */
	/* TODO (david) - Given that clients would come in thru the API methods
	 * createCopyTarget and createMOveTarget, there does not appear to be any reason
	 * why this method is part of the API. Should be package-private or private.
	 */
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

    /* TODO (david) - For creating placeholders it should be sufficient to have
     * clients supply a concrete ASTNode node type. If they want to insert a name,
     * they are free to specify either ASTNode.SIMPLE_NAME or ASTNode.QUALIFIED_NAME;
     * either would do equally well. As long as the rewriter can tell that its a 
     * placeholder, it should be able to deal with it when the time comes.
     * This allows you to delete the API constants declared on this class.
     */			
	/**
	 * Creates and returns a placeholder node for a source string that is to be inserted into
	 * the output document at the position corresponding to the placeholder.
	 * The string will be inserted without being reformatted beyond correcting
	 * the indentation level. The placeholder node can either be inserted as new or
	 * used to replace an existing node.
	 * 
	 * @param code the string to be inserted; lines should should not have extra indentation
	 * @param nodeType the type of the placeholder; 
	 * valid values are <code>METHOD_DECLARATION</code>,
	 * <code>FIELD_DECLARATION</code>, <code>INITIALIZER</code>,
	 * <code>TYPE_DECLARATION</code>, <code>BLOCK</code>, <code>STATEMENT</code>,
	 *  <code>SINGLEVAR_DECLARATION</code>,<code> VAR_DECLARATION_FRAGMENT</code>,
	 * <code>TYPE</code>, <code>EXPRESSION</code>, <code>NAME</code>
	 * <code>PACKAGE_DECLARATION</code>, <code>IMPORT_DECLARATION</code>
	 * and <code>JAVADOC</code>
	 * @return the new placeholder node
	 * @throws IllegalArgumentException if the code is null, or if the node
	 * type is invalid
	 */
	public final ASTNode createStringPlaceholder(String code, int nodeType) {
		if (code == null) {
			throw new IllegalArgumentException();
		}
		ASTNode placeholder= fNodeStore.newPlaceholderNode(nodeType);
		if (placeholder == null) {
			throw new IllegalArgumentException("String placeholder is not supported for type" + nodeType); //$NON-NLS-1$
		}
		
		fNodeStore.markAsStringPlaceholder(placeholder, code);
		return placeholder;
	}
	
	/* TODO (david) - Given that clients would come in thru the API methods
	 * createCopyTarget and createMoveTarget, there does not appear to be any reason
	 * why this method is part of the API. Should be package-private or private.
	 */
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
		if (node == null) {
			throw new IllegalArgumentException();
		}
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
		if (node == null) {
			throw new IllegalArgumentException();
		}
		return createTargetNode(node, true);
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
		if (fEventStore != null) {
			buf.append(fEventStore.toString());
		}
		return buf.toString();
	}
}
