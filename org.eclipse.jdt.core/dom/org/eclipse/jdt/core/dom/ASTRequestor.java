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

import org.eclipse.jdt.core.ICompilationUnit;

/**
 * An AST requestor provides the sources to an {@link ASTParser AST parser} that creates abstract syntax trees
 * and reports them to this AST requestor.
 * <p>
 * This class is intended to be subclassed by clients.
 * </p><p>
 * Note that this API is under development and suggest to change without notice.
 * </p>
 * 
 * @see ASTParser#createASTs(ASTRequestor, org.eclipse.core.runtime.IProgressMonitor)
 * @since 3.1
 */
// TODO (jerome) remove statement about API being under development above
public abstract class ASTRequestor {
	
	/**
	 * Accepts an abstract syntax tree.
	 * 
	 * @param node the abtract syntax tree to be accepted
	 */
	public abstract void acceptAST(ASTNode node);

	/**
	 * Returns the compilation units that need to have their abstract syntax trees created.
	 * Once all compilation units have been processed, another call to this method is done.
	 * If it returns a non-<code>null</code> value, the new compilation units are processed
	 * until this method returns <code>null</code>.
	 * <p>
	 * All compilation units in this set, or in a previous set must pertain to the same project.
	 * </p><p>
	 * Note that a compilation unit whose AST has been created and accepted by this requestor
	 * cannot be handled a second time.
	 * </p>
	 * 
	 * @return the compilation units to process, or <code>null</code> if none remains to be processed.
	 */
	public abstract ICompilationUnit[] getSources();
}
