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

import java.util.Iterator;
import java.util.List;

/**
 * Local variable declaration expression AST node type.
 * <p>
 * This kind of node collects together several variable declaration fragments
 * (<code>VariableDeclarationFragment</code>) into a single expression
 * (<code>Expression</code>), all sharing the same modifiers and base type.
 * This type of node is used (only) as the initializer of a
 * <code>ForStatement</code>.
 * </p>
 * For 2.0 (corresponding to JLS2):
 * <pre>
 * VariableDeclarationExpression:
 *    { Modifier } Type VariableDeclarationFragment
 *         { <b>,</b> VariableDeclarationFragment } 
 * </pre>
 * For 3.0 (corresponding to JLS3), the modifier flags were replaced by
 * a list of modifier nodes (intermixed with annotations):
 * <pre>
 * VariableDeclarationExpression:
 *    { ExtendedModifier } Type VariableDeclarationFragment
 *         { <b>,</b> VariableDeclarationFragment } 
 * </pre>
 * 
 * @since 2.0
 */
public class VariableDeclarationExpression extends Expression {

	/**
	 * The extended modifiers (element type: <code>ExtendedModifier</code>). 
	 * Null in 2.0. Added in 3.0; defaults to an empty list
	 * (see constructor).
	 * @since 3.0
	 */
	private ASTNode.NodeList modifiers = null;
	
	/**
	 * The modifier flags; bit-wise or of Modifier flags.
	 * Defaults to none. Not used in 3.0.
	 */
	private int modifierFlags = Modifier.NONE;
		
	/**
	 * The base type; lazily initialized; defaults to an unspecified,
	 * legal type.
	 */
	private Type baseType = null;

	/**
	 * The list of variable declaration fragments (element type: 
	 * <code VariableDeclarationFragment</code>).  Defaults to an empty list.
	 */
	private ASTNode.NodeList variableDeclarationFragments = 
		new ASTNode.NodeList(true,  VariableDeclarationFragment.class);

	/**
	 * Creates a new unparented local variable declaration expression node
	 * owned by the given AST.  By default, the variable declaration has: no
	 * modifiers, an unspecified (but legal) type, and an empty list of variable
	 * declaration fragments (which is syntactically illegal).
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	VariableDeclarationExpression(AST ast) {
		super(ast);
		if (ast.API_LEVEL >= AST.LEVEL_3_0) {
			this.modifiers = new ASTNode.NodeList(true, ExtendedModifier.class);
		}
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return VARIABLE_DECLARATION_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		VariableDeclarationExpression result = 
			new VariableDeclarationExpression(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		if (getAST().API_LEVEL == AST.LEVEL_2_0) {
			result.setModifiers(getModifiers());
		}
		if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
			result.modifiers().addAll(ASTNode.copySubtrees(target, modifiers()));
		}
		result.setType((Type) getType().clone(target));
		result.fragments().addAll(
			ASTNode.copySubtrees(target, fragments()));
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
			if (getAST().API_LEVEL >= AST.LEVEL_3_0) {
				acceptChildren(visitor, this.modifiers);
			}
			acceptChild(visitor, getType());
			acceptChildren(visitor, variableDeclarationFragments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of modifiers and annotations
	 * of this declaration (added in 3.0 API).
	 * <p>
	 * Note that the final modifier is the only meaningful modifier for local
	 * variable declarations.
	 * </p>
	 * <p>
	 * Note: Support for annotation metadata is an experimental language feature 
	 * under discussion in JSR-175 and under consideration for inclusion
	 * in the 1.5 release of J2SE. The support here is therefore tentative
	 * and subject to change.
	 * </p>
	 * 
	 * @return the live list of modifiers and annotations
	 *    (element type: <code>ExtendedModifier</code>)
	 * @exception UnsupportedOperationException if this operation is used in
	 * a 2.0 AST
	 * @since 3.0
	 */ 
	public List modifiers() {
		// more efficient than just calling unsupportedIn2() to check
		if (this.modifiers == null) {
			unsupportedIn2();
		}
		return this.modifiers;
	}
	
	/**
	 * Returns the modifiers explicitly specified on this declaration.
	 * <p>
	 * In the 3.0 API, this method is a convenience method that
	 * computes these flags from <code>modifiers()</code>.
	 * </p>
	 * 
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see Modifier
	 */ 
	public int getModifiers() {
		// more efficient than checking getAST().API_LEVEL
		if (this.modifiers == null) {
			// 2.0 behavior - bona fide property
			return this.modifierFlags;
		} else {
			// 3.0 behavior - convenient method
			// performance could be improved by caching computed flags
			// but this would require tracking changes to this.modifiers
			int flags = Modifier.NONE;
			for (Iterator it = modifiers().iterator(); it.hasNext(); ) {
				Object x = it.next();
				if (x instanceof Modifier) {
					flags |= ((Modifier) x).getKeyword().toFlagValue();
				}
			}
			return flags;
		}
	}

	/**
	 * Sets the modifiers explicitly specified on this declaration (2.0 API only).
	 * <p>
	 * Note that the final modifier is the only meaningful modifier for local
	 * variable declarations.
	 * </p>
	 * 
	 * @param modifiers the given modifiers (bit-wise or of <code>Modifier</code> constants)
	 * @exception UnsupportedOperationException if this operation is used in
	 * an AST later than 2.0
	 * @see Modifier
	 * TBD (jeem ) - deprecated In the 3.0 API, this method is replaced by 
	 * <code>modifiers()</code> which contains a list of 
	 * a <code>Modifier</code> nodes.
	 */ 
	public void setModifiers(int modifiers) {
	    supportedOnlyIn2();
		modifying();
		this.modifierFlags = modifiers;
	}

	/**
	 * Returns the base type declared in this variable declaration.
	 * <p>
	 * N.B. The individual child variable declaration fragments may specify 
	 * additional array dimensions. So the type of the variable are not 
	 * necessarily exactly this type.
	 * </p>
	 * 
	 * @return the base type
	 */ 
	public Type getType() {
		if (this.baseType == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setType(getAST().newPrimitiveType(PrimitiveType.INT));
			getAST().setModificationCount(count);
		}
		return this.baseType;
	}

	/**
	 * Sets the base type declared in this variable declaration to the given
	 * type.
	 * 
	 * @param type the new base type
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
		replaceChild(this.baseType, type, false);
		this.baseType = type;
	}

	/**
	 * Returns the live list of variable declaration fragments in this 
	 * expression. Adding and removing nodes from this list affects this node
	 * dynamically. All nodes in this list must be 
	 * <code>VariableDeclarationFragment</code>s; attempts to add any other
	 * type of node will trigger an exception.
	 * 
	 * @return the live list of variable declaration fragments in this 
	 *    expression (element type: <code>VariableDeclarationFragment</code>)
	 */ 
	public List fragments() {
		return this.variableDeclarationFragments;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 4 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (this.modifiers == null ? 0 : this.modifiers.listSize())
			+ (this.baseType == null ? 0 : getType().treeSize())
			+ this.variableDeclarationFragments.listSize();
	}
}

