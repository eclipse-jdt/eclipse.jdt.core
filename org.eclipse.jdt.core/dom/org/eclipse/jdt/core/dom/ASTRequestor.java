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
 * @see ASTParser#createASTs(ASTRequestor, org.eclipse.core.runtime.IProgressMonitor)
 * @since 3.1
 */
// TODO (jerome) remove statement about API being under development above
public abstract class ASTRequestor {
	
	/**
	 * Accepts an AST. The AST is either for one of the compilation units
	 * included in the result of an earlier call to {@link #getSources()},
	 * or a compilation unit in the same project that also needed to
	 * be parsed in the course of resolving bindings.
	 * <p>
	 * [TODO (jerome) issue: Consider passing ICompilationUnit as the first parameter
	 * to this method. This would make it for clients to track which
	 * compilation units have been done, since it is an error to
	 * ask for the same compilation unit again.]
	 * </p>
	 * <p>
	 * [TODO (jerome) issue: The parameter type could be CompilationUnit
	 * rather than ASTNode as long as ASTParser.setKind(K_COMPILATION_UNIT)
	 * is always used.]
	 * </p>
	 * 
	 * @param node the abtract syntax tree to be accepted
	 */
	public abstract void acceptAST(ASTNode node);

	/**
	 * Returns the compilation units for which ASTs should be created.
	 * All of the compilation units must belong to the same project,
	 * and there must not be any duplicates.
	 * <p>
	 * [TODO (jerome) issue: It would make sense to return an empty list
	 * rather than null when there are no more compilation units to
	 * process.]
	 * </p>
	 * <p>
	 * [TODO (jerome) issue: It would simplify clients if requesting a compilation
	 * unit that had already been processed were ignored rather than
	 * being considered an error. Otherwise some clients would
	 * have to maintain a list of compilation units they'd accepted.]
	 * </p>
	 * 
	 * @return the compilation units to process, or <code>null</code> if none
	 */
	public abstract ICompilationUnit[] getSources();
}
