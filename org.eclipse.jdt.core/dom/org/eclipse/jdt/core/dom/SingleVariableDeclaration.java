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
 * Single variable declaration AST node type. Single variable
 * declaration nodes are used in a limited number of places, including formal
 * parameter lists and catch clauses. They are not used for field declarations
 * and regular variable declaration statements.
 *
 * <pre>
 * SingleVariableDeclaration:
 *    { Modifier } Type Identifier { <b>[</b><b>]</b> } [ <b>=</b> Expression ]
 * </pre>
 * 
 * @since 2.0
 */
public class SingleVariableDeclaration extends VariableDeclaration {
	
	/**
	 * Mask containing all legal modifiers for this construct.
	 */
	private static final int LEGAL_MODIFIERS = 
		Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
		| Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE
		| Modifier.TRANSIENT;

	/**
	 * The modifiers; bit-wise or of Modifier flags.
	 * Defaults to none.
	 */
	private int modifiers = Modifier.NONE;
	
	/**
	 * The variable name; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private SimpleName variableName = null;

	/**
	 * The type; lazily initialized; defaults to a unspecified,
	 * legal type.
	 */
	private Type type = null;

	/**
	 * The number of extra array dimensions that appear after the variable;
	 * defaults to 0.
	 * 
	 * @since 2.1
	 */
	private int extraArrayDimensions = 0;

	/**
	 * The initializer expression, or <code>null</code> if none;
	 * defaults to none.
	 */
	private Expression optionalInitializer = null;

	/**
	 * Creates a new AST node for a variable declaration owned by the given 
	 * AST. By default, the variable declaration has: no modifiers, an 
	 * unspecified (but legal) type, an unspecified (but legal) variable name, 
	 * 0 dimensions after the variable; no initializer.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	SingleVariableDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SINGLE_VARIABLE_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SingleVariableDeclaration result = new SingleVariableDeclaration(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setModifiers(getModifiers());
		result.setType((Type) getType().clone(target));
		result.setExtraDimensions(getExtraDimensions());
		result.setName((SimpleName) getName().clone(target));
		result.setInitializer(
			(Expression) ASTNode.copySubtree(target, getInitializer()));
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
			acceptChild(visitor, getInitializer());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 * Note that the final modifier is the only meaningful modifier for local
	 * variable and formal parameter declarations.
	 * </p>
	 * 
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see Modifier
	 */ 
	public int getModifiers() {
		return modifiers;
	}

	/**
	 * Sets the modifiers explicitly specified on this declaration.
	 * <p>
	 * The following modifiers are valid for fields: public, private, protected,
	 * static, final, volatile, and transient. For local variable and formal
	 * parameter declarations, the only meaningful modifier is final.
	 * </p>
	 * 
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see Modifier
	 * @exception IllegalArgumentException if the modifiers are illegal
	 */ 
	public void setModifiers(int modifiers) {
		if ((modifiers & ~LEGAL_MODIFIERS) != 0) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.modifiers = modifiers;
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
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
		
	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public void setName(SimpleName variableName) {
		if (variableName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.variableName, variableName, false);
		this.variableName = variableName;
	}

	/**
	 * Returns the type of the variable declared in this variable declaration,
	 * exclusive of any extra array dimensions.
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
	 * Sets the type of the variable declared in this variable declaration to 
	 * the given type, exclusive of any extra array dimensions.
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
		replaceChild(this.type, type, false);
		this.type = type;
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 * @since 2.1
	 */ 
	public int getExtraDimensions() {
		return extraArrayDimensions;
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 * @since 2.1
	 */ 
	public void setExtraDimensions(int dimensions) {
		if (dimensions < 0) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.extraArrayDimensions = dimensions;
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public Expression getInitializer() {
		return optionalInitializer;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public void setInitializer(Expression initializer) {
		// a SingleVariableDeclaration may occur inside an Expression 
		// must check cycles
		replaceChild(this.optionalInitializer, initializer, true);
		this.optionalInitializer = initializer;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 5 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (type == null ? 0 : getType().treeSize())
			+ (variableName == null ? 0 : getName().treeSize())
			+ (optionalInitializer == null ? 0 : getInitializer().treeSize());
	}
}
