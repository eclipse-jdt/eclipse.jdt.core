/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * Abstract base class of all AST node types that declare a single local 
 * variable.
 * <p>
 * <pre>
 * VariableDeclaration:
 *    SingleVariableDeclaration
 *    VariableDeclarationFragment
 * </pre>
 * </p>
 * 
 * @see SingleVariableDeclaration
 * @see VariableDeclarationFragment
 * @since 2.0
 */
public abstract class VariableDeclaration extends ASTNode {
	
	/**
	 * Creates a new AST node for a variable declaration owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	VariableDeclaration(AST ast) {
		super(ast);
	}
	
	/**
	 * Resolves and returns the binding for the variable declared in this
	 * variable declaration.
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
}
