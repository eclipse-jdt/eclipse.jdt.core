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

package org.eclipse.jdt.core.dom;

/**
 * Static or instance initializer AST node type.
 * <pre>
 * Initializer:
 *     { <b>static</b> } Block
 * </pre>
 * 
 * @since 2.0
 */
public class Initializer extends BodyDeclaration {
	
	/**
	 * The initializer body; lazily initialized; defaults to an empty block.
	 */
	private Block body = null;

	/**
	 * Creates a new AST node for an initializer declaration owned by the given 
	 * AST. By default, the initializer has no modifiers and an empty block.
	 * The javadoc comment is not used for initializers.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Initializer(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return INITIALIZER;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		Initializer result = new Initializer(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		if (getAST().API_LEVEL == AST.LEVEL_2_0) {
			result.setModifiers(getModifiers());
		}
		if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		}
		result.setJavadoc(
			(Javadoc) ASTNode.copySubtree(target, getJavadoc()));
		result.setBody((Block) getBody().clone(target));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getJavadoc());
			if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
				acceptChildren(visitor, this.modifiers);
			}
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the body of this initializer declaration.
	 * 
	 * @return the initializer body
	 */ 
	public Block getBody() {
		if (this.body == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setBody(new Block(getAST()));
			getAST().setModificationCount(count);
		}
		return this.body;
	}
	
	/**
	 * Sets the body of this initializer declaration.
	 * 
	 * @param body the block node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block body) {
		if (body == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.body, body, true);
		this.body = body;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize())
			+ (this.modifiers == null ? 0 : this.modifiers.listSize())
			+ (this.body == null ? 0 : getBody().treeSize());
	}
}

