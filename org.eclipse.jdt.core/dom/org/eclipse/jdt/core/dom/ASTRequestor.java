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
	 * The default implementation of this method does nothing.
	 * Clients should override if additional asts are of interest.
	 * </p>
	 * 
	 * @param source the compilation unit the ast is coming from
	 * @param ast the requested abtract syntax tree
	 */
	public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
		acceptAST(ast, source);
	}
	
	/**
	 * @deprecated use acceptAST(ICompilationUnit, CompilationUnit) instead
	 */
	// TODO (jerome) remove when no more clients
	public void acceptAST(CompilationUnit ast, ICompilationUnit source) {
		// do nothing
	}
	
	/**
	 * Accepts a binding corresponding to the binding key.
	 * That is, <code>binding</code> is an binding for 
	 * <code>bindingKey</code>.
	 * <p>
	 * The default implementation of this method does nothing.
	 * Clients should override if additional bindings are of interest.
	 * </p>
	 * 
	 * @param bindingKey the key of the requested binding
	 * @param binding the requested binding, or <code>null</code> if none
	 */
	public void acceptBinding(String bindingKey, IBinding binding) {
		acceptBinding(binding, bindingKey);
	}

	/**
	 * @deprecated use acceptBinding(String, IBinding) instead
	 */
	// TODO (jerome) remove when no more clients
	public void acceptBinding(IBinding binding, String bindingKey) {
		// do nothing
	}
	
	/**
	 * Resolves bindings for the given binding keys.
	 * The given binding keys must have been obtained using {@link IBinding#getKey()}.
	 * <p>
	 * If a binding key cannot be resolved, <code>null</code> is put in the resulting array.
	 * </p>
	 * The resulting binding is undefined for a binding key representing a local element if the corresponding
	 * ast or another binding key in the same compilation unit was also requested by
	 * {@link ASTParser#createASTs(ICompilationUnit[], String[], ASTRequestor, IProgressMonitor)}.
	 * </p>
	 * 
	 * @param bindingKeys the keys of bindings to create
	 * @return the created bindings
	 * @see ASTParser#createASTs(ICompilationUnit[], String[], ASTRequestor, IProgressMonitor)
	 */
	public IBinding[] createBindings(String[] bindingKeys) {
		int length = bindingKeys.length;
		IBinding[] result = new IBinding[length];
		for (int i = 0; i < length; i++)
			result[i] = this.compilationUnitResolver.createBinding(bindingKeys[i]);
		return result;
	}
}
