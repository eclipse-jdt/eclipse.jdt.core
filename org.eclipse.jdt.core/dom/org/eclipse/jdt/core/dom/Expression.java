/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * Abstract base class of AST nodes that represent expressions.
 * There are several kinds of expressions.
 * <p>
 * <pre>
 * Expression:
 *    Name
 *    IntegerLiteral (includes decimal, hex, and octal forms; and long)
 *    FloatingPointLiteral (includes both float and double)
 *    CharacterLiteral
 *    NullLiteral
 *    BooleanLiteral
 *    StringLiteral
 *    TypeLiteral
 *    ThisExpression
 *    SuperFieldAccess
 *    FieldAccess
 *    Assignment
 *    ParenthesizedExpression
 *    ClassInstanceCreation
 *    ArrayCreation
 *    ArrayInitializer
 *    MethodInvocation
 *    SuperMethodInvocation
 *    ArrayAccess
 *    InfixExpression
 *    InstanceofExpression
 *    ConditionalExpression
 *    PostfixExpression
 *    PrefixExpression
 *    CastExpression
 *    VariableDeclarationExpression
 * </pre>
 * </p>
 * 
 * @since 2.0
 */
public abstract class Expression extends ASTNode {
	
	/**
	 * Creates a new AST node for an expression owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Expression(AST ast) {
		super(ast);
	}
	
	/**
	 * Resolves and returns the binding for the type of this expression.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding for the type of this expression, or
	 *    <code>null</code> if the type cannot be resolved
	 */	
	public final ITypeBinding resolveTypeBinding() {
		return this.ast.getBindingResolver().resolveExpressionType(this);
	}
	
	/**
	 * Returns whether this expression node is the site of a boxing
	 * conversion (JLS3 5.1.7). This information is available only
	 * when bindings are requested when the AST is being built.
	 * 
	 * @return <code>true</code> if this expression is the site of a
	 * boxing conversion, or <code>false</code> if either no boxing conversion
	 * is involved or if bindings were not requested when the AST was created
	 * @since 3.1
	 */
	public final boolean resolveBoxing() {
		// TODO (olivier) - missing implementation
		return false;
	}
	
	/**
	 * Returns whether this expression node is the site of an unboxing
	 * conversion (JLS3 5.1.8). This information is available only
	 * when bindings are requested when the AST is being built.
	 * 
	 * @return <code>true</code> if this expression is the site of an
	 * unboxing conversion, or <code>false</code> if either no unboxing
	 * conversion is involved or if bindings were not requested when the
	 * AST was created
	 * @since 3.1
	 */
	public final boolean resolveUnboxing() {
		// TODO (olivier) - missing implementation
		return false;
	}
	
}

