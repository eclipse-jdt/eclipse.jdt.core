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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;

/**
 * An AST requestor handles ASTs for compilation units passed to <code>ASTParser.createASTs</code>.
 * <p>
 * This class is intended to be subclassed by clients.
 * </p>
 * <p>
 * Note that this API is under development and subject to change without notice.
 * </p>
 * 
 * @see ASTParser#createASTs(ICompilationUnit[], String[], ASTRequestor, org.eclipse.core.runtime.IProgressMonitor)
 * @since 3.1
 */
// TODO (jerome) remove statement about API being under development above
public abstract class ASTRequestor {
	
	/**
	 * The compilation unit resolver used to resolve bindings.
	 */
	CompilationUnitResolver compilationUnitResolver;
	
	
	/**
	 * Accepts an AST corresponding to the compilation unit.
	 * That is, <code>ast</code> is an AST for <code>source</code>.
	 * <p>
	 * [TODO (jerome) issue: It would be more consistent if the input (source) was first,
	 * and the unknown (ast) was second.]
	 * </p>
	 * 
	 * @param ast the requested abtract syntax tree
	 * @param source the compilation unit the ast is coming from
	 */
	public void acceptAST(CompilationUnit ast, ICompilationUnit source) {
		// TODO (jerome) method will be abstract when clients don't use acceptAST(ASTNode) any longer
		acceptAST(ast);
	}
	
	/**
	 * Accepts a binding corresponding to the binding key.
	 * That is, <code>binding</code> is an binding for 
	 * <code>bindingKey</code>.
	 * <p>
	 * The default implementation of this method does nothing.
	 * Clients should override if additional bindings are of interest.
	 * </p>
	 * <p>
	 * [TODO (jerome) issue: It would be more consistent if the input (key) was first,
	 * and the unknown (binding) was second.]
	 * </p>
	 * 
	 * @param binding the requested binding, or <code>null</code> if none
	 * @param bindingKey the key of the requested binding
	 */
	public void acceptBinding(IBinding binding, String bindingKey) {
		// do nothing
	}
	
	/**
	 * @deprecated
	 */
	public void acceptAST(ASTNode node) {
		// TODO (jerome) remove method when no more clients
	}
	
	/**
	 * Resolves bindings for the given binding keys.
	 * The given binding keys must represent declarations of elements. 
	 * The source of one these elements cannot be included in the sources that are being requested
	 * or in the source of the bindings that are being requested.
	 * <p>
	 * [TODO (jerome) issue: The spec needs to be clarified. 
	 * My expectation was that the requestor could ask for any bindings they wanted.]
	 * </p>
	 * <p>
	 * [TODO (jerome) issue: Need to spec what happens if the binding key cannot
	 * be resolved. Do you just get a null in the result?  Given what the way 
	 * the requestor works, maybe we could just call acceptBinding instead.]
	 * </p>
	 * 
	 * @param bindingKeys the keys of bindings to create
	 * @return the created bindings
	 * @see ASTParser#createASTs(ICompilationUnit[], String[], ASTRequestor, IProgressMonitor)
	 */
	public IBinding[] createBindings(String[] bindingKeys) {
		return this.compilationUnitResolver.createBindings(bindingKeys);
	}
	
	/**
	 * @deprecated
	 */
	// TODO (jerome) remove method when no more clients
	public ICompilationUnit[] getSources() {
		return new ICompilationUnit[] {};
	}

}
