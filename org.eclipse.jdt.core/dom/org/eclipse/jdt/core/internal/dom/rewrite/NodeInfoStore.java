/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.internal.dom.rewrite;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.internal.dom.rewrite.RewriteEventStore.CopySourceInfo;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

/**
 *
 */
public final class NodeInfoStore {
	
	/** Constant used to create place holder nodes */
	public static final int UNKNOWN= -1;
	public static final int BLOCK= 2;
	public static final int EXPRESSION= 3;
	public static final int STATEMENT= 4;
	public static final int SINGLEVAR_DECLARATION= 5;
	public static final int TYPE= 6;
	public static final int NAME= 7;
	public static final int JAVADOC= 8;
	public static final int VAR_DECLARATION_FRAGMENT= 9;
	public static final int TYPE_DECLARATION= 10;
	public static final int FIELD_DECLARATION= 11;
	public static final int METHOD_DECLARATION= 12;
	public static final int INITIALIZER= 13;
	public static final int PACKAGE_DECLARATION= 14;
	public static final int IMPORT_DECLARATION= 15;
	public static final int METHOD_REF_PARAMETER= 16;
	public static final int TAG_ELEMENT= 17;
	
	private AST fAst;
	
	private Map fPlaceholderNodes;
	private Set fCollapsedNodes;

	public NodeInfoStore(AST ast) {
		super();
		fAst= ast;
		fPlaceholderNodes= null;
		fCollapsedNodes= null;
	}
	
	/**
	 * Returns the node type that should be used to create a place holder for the given node
	 * <code>existingNode</code>.
	 * 
	 * @param existingNode an existing node for which a place holder is to be created
	 * @return the node type of a potential place holder
	 */
	public static int getPlaceholderType(ASTNode existingNode) {
		switch (existingNode.getNodeType()) {
			case ASTNode.SIMPLE_NAME:
			case ASTNode.QUALIFIED_NAME:
				return NAME;
			case ASTNode.SIMPLE_TYPE:
			case ASTNode.PRIMITIVE_TYPE:
			case ASTNode.ARRAY_TYPE:
				return TYPE;				
			case ASTNode.BLOCK:
				return BLOCK;
			case ASTNode.TYPE_DECLARATION:
				return TYPE_DECLARATION;
			case ASTNode.METHOD_DECLARATION:
				return METHOD_DECLARATION;
			case ASTNode.FIELD_DECLARATION:
				return FIELD_DECLARATION;
			case ASTNode.INITIALIZER:
				return INITIALIZER;
			case ASTNode.SINGLE_VARIABLE_DECLARATION:
				return SINGLEVAR_DECLARATION;			
			case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
				return VAR_DECLARATION_FRAGMENT;
			case ASTNode.JAVADOC:
				return JAVADOC;
			case ASTNode.METHOD_REF_PARAMETER:
				return METHOD_REF_PARAMETER;
			case ASTNode.TAG_ELEMENT:
				return TAG_ELEMENT;
			case ASTNode.PACKAGE_DECLARATION:
				return PACKAGE_DECLARATION;
			case ASTNode.IMPORT_DECLARATION:
				return IMPORT_DECLARATION;
			default:
				if (existingNode instanceof Expression) {
					return EXPRESSION;
				} else if (existingNode instanceof Statement) {
					// is not Block: special case statement for block
					return STATEMENT;
				}
		}
		return UNKNOWN;
	}
	

	/**
	 * Marks a node as a placehoder for a plain string content. The type of the node should correspond to the
	 * code's code content.
	 * @param placeholder The placeholder node that acts for the string content.
	 * @param code The string content.
	 */
	public final void markAsStringPlaceholder(ASTNode placeholder, String code) {
		StringPlaceholderData data= new StringPlaceholderData();
		data.code= code;
		setPlaceholderData(placeholder, data);
	}
	
	/**
	 * Marks a node as a copy or move target. The copy target represents a copied node at the target (copied) site.
	 * @param target The node at the target site. Can be a placeholder node but also the source node itself.
	 * @param copySource The info at the source site.
	 */
	public final void markAsCopyTarget(ASTNode target, CopySourceInfo copySource) {
		CopyPlaceholderData data= new CopyPlaceholderData();
		data.copySource= copySource;
		setPlaceholderData(target, data);
	}
		
	/**
	 * Creates a placeholder node of the given type. <code>null</code> if the type is not supported
	 * @param nodeType Type of the node to create. Use the type constants in {@link NodeInfoStore}.
	 * @return Returns a place holder node.
	 */
	public final ASTNode newPlaceholderNode(int nodeType) {
		AST ast= fAst;
		switch (nodeType) {
			case NAME:
				return ast.newSimpleName("z"); //$NON-NLS-1$
			case EXPRESSION:
				MethodInvocation expression = ast.newMethodInvocation(); 
				expression.setName(ast.newSimpleName("z")); //$NON-NLS-1$
				return expression;
			case TYPE:
				return ast.newSimpleType(ast.newSimpleName("X")); //$NON-NLS-1$		
			case STATEMENT:
				return ast.newReturnStatement();
			case BLOCK:
				return ast.newBlock();
			case METHOD_DECLARATION:
				return ast.newMethodDeclaration();
			case FIELD_DECLARATION:
				return ast.newFieldDeclaration(ast.newVariableDeclarationFragment());
			case INITIALIZER:
				return ast.newInitializer();				
			case SINGLEVAR_DECLARATION:
				return ast.newSingleVariableDeclaration();
			case VAR_DECLARATION_FRAGMENT:
				return ast.newVariableDeclarationFragment();
			case JAVADOC:
				return ast.newJavadoc();
			case METHOD_REF_PARAMETER:
				return ast.newMethodRefParameter();
			case TAG_ELEMENT:
				return ast.newTagElement();
			case TYPE_DECLARATION:
				return ast.newTypeDeclaration();
			case PACKAGE_DECLARATION:
				return ast.newPackageDeclaration();
			case IMPORT_DECLARATION:
				return ast.newImportDeclaration();
		}
		return null;
	}	


	
	
	// collapsed nodes: in source: use one node that represents many; to be used as
	// copy/move source or to replace at once.
	// in the target: one block node that is not flattened.
	
	public Block createCollapsePlaceholder() {
		Block placeHolder= fAst.newBlock();
		if (fCollapsedNodes == null) {
			fCollapsedNodes= new HashSet();
		}
		fCollapsedNodes.add(placeHolder);
		return placeHolder;
	}
	
	public boolean isCollapsed(ASTNode node) {
		if (fCollapsedNodes != null) {
			return fCollapsedNodes.contains(node);
		}
		return false;	
	}
	
	public Object getPlaceholderData(ASTNode node) {
		if (fPlaceholderNodes != null) {
			return fPlaceholderNodes.get(node);
		}
		return null;	
	}
	
	private void setPlaceholderData(ASTNode node, PlaceholderData data) {
		if (fPlaceholderNodes == null) {
			fPlaceholderNodes= new IdentityHashMap();
		}
		fPlaceholderNodes.put(node, data);		
	}
	
	private static class PlaceholderData {
		// don't allow to create such an instance
	}
			
	protected static final class CopyPlaceholderData extends PlaceholderData {
		public CopySourceInfo copySource;
		public String toString() {
			return "[placeholder " + copySource +"]";  //$NON-NLS-1$//$NON-NLS-2$
		}
	}	
	
	protected static final class StringPlaceholderData extends PlaceholderData {
		public String code;
		public String toString() {
			return "[placeholder string: " + code +"]"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	
	/**
	 * 
	 */
	public void clear() {
		fPlaceholderNodes= null;
		fCollapsedNodes= null;
	}
}
