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
 * AST node for a parameter within a method reference ({@link MethodRef MethodRef}).
 * These nodes only occur within doc comments ({@link Javadoc Javadoc}).
 * <pre>
 * MethodRefParameter:
 * 		Type [ Identifier ]
 * </pre>
 * 
 * @see Javadoc
 * @since 3.0
 */
public class MethodRefParameter extends ASTNode {
	
	/**
	 * The type; lazily initialized; defaults to a unspecified,
	 * legal type.
	 */
	private Type type = null;

	/**
	 * The parameter name, or <code>null</code> if none; none by
	 * default.
	 */
	private SimpleName optionalParameterName = null;

	/**
	 * Creates a new AST node for a method referenece parameter owned by the given 
	 * AST. By default, the node has an unspecified (but legal) type, 
	 * and no parameter name.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	MethodRefParameter(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return METHOD_REF_PARAMETER;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		MethodRefParameter result = new MethodRefParameter(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setType((Type) ASTNode.copySubtree(target, getType()));
		result.setName((SimpleName) ASTNode.copySubtree(target, getName()));
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
			// visit children in normal left to right reading order
			acceptChild(visitor, getType());
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the paramter type.
	 * 
	 * @return the parameter type
	 */ 
	public Type getType() {
		if (this.type == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setType(getAST().newPrimitiveType(PrimitiveType.INT));
			getAST().setModificationCount(count);
		}
		return this.type;
	}

	/**
	 * Sets the paramter type to the given type.
	 * 
	 * @param type the new type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the type is <code>null</code></li>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.type, type, false);
		this.type = type;
	}

	/**
	 * Returns the parameter name, or <code>null</code> if there is none.
	 * 
	 * @return the parameter name node, or <code>null</code> if there is none
	 */ 
	public SimpleName getName() {
		return this.optionalParameterName;
	}
	
	/**
	 * Sets or clears the parameter name.
	 * 
	 * @param name the parameter name node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName name) {
		// a MethodRef cannot occur inside a SimpleName - no cycle check
		replaceChild(this.optionalParameterName, name, false);
		this.optionalParameterName = name;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (this.type == null ? 0 : getType().treeSize())
			+ (this.optionalParameterName == null ? 0 : getName().treeSize());
	}
}
