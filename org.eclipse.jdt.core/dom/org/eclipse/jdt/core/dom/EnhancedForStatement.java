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

package org.eclipse.jdt.core.dom;

/**
 * Enhanced For statement AST node type (added in 3.0 API).
 *
 * <pre>
 * EnhancedForStatement:
 *    <b>for</b> <b>(</b> Type Identifier<b>:</b> Expression <b>)</b>
 * 			Statement
 * </pre>
 * 
 * <p>
 * Note: Enhanced for statements are an experimental language feature 
 * under discussion in JSR-201 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * @since 3.0
 */
public class EnhancedForStatement extends Statement {
	
	/**
	 * The type; lazily initialized; defaults to a unspecified,
	 * legal type.
	 */
	private Type type = null;

	/**
	 * The variable name; lazily initialized; defaults to an unspecified,
	 * legal Java identifier.
	 */
	private SimpleName variableName = null;

	/**
	 * The expression; lazily initialized; defaults to a unspecified, but legal,
	 * expression.
	 */
	private Expression expression = null;

	/**
	 * The body statement; lazily initialized; defaults to an empty block
	 * statement.
	 */
	private Statement body = null;
			
	/**
	 * Creates a new AST node for an enchanced for statement owned by the
	 * given AST. By default, the type, name, and expression are unspecified
	 * but legal subtrees, and the body is an empty block.
	 * 
	 * @param ast the AST that is to own this node
	 */
	EnhancedForStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ENHANCED_FOR_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		EnhancedForStatement result = new EnhancedForStatement(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.copyLeadingComment(this);
		result.setType((Type) getType().clone(target));
		result.setName((SimpleName) getName().clone(target));
		result.setExpression((Expression) getExpression().clone(target));
		result.setBody(
			(Statement) ASTNode.copySubtree(target, getBody()));
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
			acceptChild(visitor, getExpression());
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the type in this enhanced for statement.
	 * 
	 * @return the type
	 */ 
	public Type getType() {
		if (type == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setType(getAST().newPrimitiveType(PrimitiveType.INT));
			getAST().setModificationCount(count);
		}
		return type;
	}

	/**
	 * Sets the type in this enhanced for statement to the given type.
	 * 
	 * @param type the new type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		// an EnchacedForStatement cannot occur inside a Type - cycles not possible
		replaceChild(this.type, type, false);
		this.type = type;
	}
	
	/**
	 * Returns the name of the variable in this enhanced for statement.
	 * 
	 * @return the variable name node
	 */ 
	public SimpleName getName() {
		if (variableName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setName(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return variableName;
	}
		
	/**
	 * Sets the name of the variable in this enhanced for statement
	 * to the given name.
	 * 
	 * @param variableName the new variable name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName variableName) {
		if (variableName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.variableName, variableName, false);
		this.variableName = variableName;
	}

	/**
	 * Returns the expression of this enhanced for statement.
	 * 
	 * @return the expression node
	 */ 
	public Expression getExpression() {
		if (expression == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setExpression(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return expression;
	}
		
	/**
	 * Sets the expression of this enhanced for statement.
	 * 
	 * @param expression the new expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setExpression(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an EnhancedForStatement may occur inside an Expression 
		// must check cycles
		replaceChild(this.expression, expression, true);
		this.expression = expression;
	}

	/**
	 * Returns the body of this enchanced for statement.
	 * 
	 * @return the body statement node
	 */ 
	public Statement getBody() {
		if (body == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setBody(new Block(getAST()));
			getAST().setModificationCount(count);
		}
		return body;
	}
	
	/**
	 * Sets the body of this enhanced for statement.
	 * 
	 * @param statement the body statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Statement statement) {
		if (statement == null) {
			throw new IllegalArgumentException();
		}
		// an EnhancedForStatement may occur inside a Statement - must check cycles
		replaceChild(this.body, statement, true);
		this.body = statement;
	}
	
	/**
	 * Resolves and returns the binding for the loop variable of this
	 * enhanced for statement.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IVariableBinding resolveBinding() {
		return getAST().getBindingResolver().resolveVariable(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 4 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (type == null ? 0 : getType().treeSize())
			+ (variableName == null ? 0 : getName().treeSize())
			+ (expression == null ? 0 : getExpression().treeSize())
			+ (body == null ? 0 : getBody().treeSize());
	}
}
