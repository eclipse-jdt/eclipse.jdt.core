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
 * An AST requestor feeds compilation units to 
 * <code>ASTParser.createASTs</code>
 * and accepts the ASTs that arise from parsing them.
 * All of the compilation units must be from the same project.
 * The requestor is passed ASTs for not only the compilation units
 * it asks for, but also any other ones in the same project that
 * also needed to be parsed in the course of resolving bindings.
 * <p>
 * The lifecycle is as follows:
 * <ul>
 * <li>requestor.getSources() is called to find out what
 * compilation units to parse</li>
 * <li>requestor.acceptAST(ASTNode) is called several times,
 * for each compilation unit in the original list, and perhaps
 * for other compilation units in the same project</li>
 * <li>requestor.getSources() is called again to find out whether
 * there are additional compilation units to parse</li>
 * <li>the last 2 steps are repeated until getSources()
 * returns null</li>
 * </ul> 
 * Note that all the compilation units involved must be from
 * the same project, and can only be processed once.
 * </p>
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
	 * 
	 */
	CompilationUnitResolver compilationUnitResolver;
	
	
	/**
	 * Accepts an AST.
	 * 
	 * @param ast the requested abtract syntax tree
	 * @param source the compilation unit the ast is coming from
	 */
	public void acceptAST(CompilationUnit ast, ICompilationUnit source) {
		// method will be abstract when clients don't use acceptAST(ASTNode) any longer
		acceptAST(ast);
	}
	
	/**
	 * Accepts a binding.
	 * 
	 * @param binding the requested binding 
	 * @param bindingKey the key of the requested binding
	 */
	public void acceptBinding(IBinding binding, String bindingKey) {
		// TODO (jerome) make abstract when clients implement it
	}
	
	/**
	 * @deprecated
	 */
	public void acceptAST(ASTNode node) {
		// TODO (jerome) remove when no more clients
	}
	
	/**
	 * Creates the bindings corresponding to the given keys.
	 * The given keys represent declarations of elements. 
	 * The source of one these elements cannot be included in the sources that are being requested
	 * or in the source of the bindings that are being requested.
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
	public ICompilationUnit[] getSources() {
		return new ICompilationUnit[] {};
	}

}
