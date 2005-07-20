/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

import org.eclipse.jdt.core.dom.ASTNode;

import com.sun.mirror.apt.Messager;
import com.sun.mirror.util.SourcePosition;

/**
 * Extend the APT {@link Messager} to allow the registrating of diagnostics on ast nodes. 
 */
public interface EclipseMessager extends Messager 
{
	/**
	 * Problem ID for APT problems that do not have quick fixes
	 */
	public static final int APT_PROBLEM_ID = 0x80889999;
	
	/** 
	 * Problem ID for APT problems that have quick fixes.
	 * If an APT plugin wants to write a quick-fix for their problems,
	 * they should look for this ID.
	 * 
	 * TODO add pointer to methods that add arguments
	 */
	public static final int APT_QUICK_FIX_PROBLEM_ID = 0x80889998;
	
	
	/**
	 * Print an error message on the given AST node. 
	 * The AST node must came from the AST that is associated with the environment.
	 * @param node
	 * @param msg the error message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 *         Also, if the node did not come from the ast in the environment.
	 */	
	void printError(ASTNode node, String msg);
	
	/**
	 * Print a warning on the given AST node.
	 * The AST node must came from the AST that is associated with the environment.
	 * @param node
	 * @param msg the warning message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 * 		   Also, if the node did not come from the ast in the environment.
	 */
	void printWarning(ASTNode node, String msg);
	
	/**
	 * Print a notice on the given AST node.
	 * The AST node must came from the AST that is associated with the environment.
	 * @param node
	 * @param msg the warning message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 *         Also, if the node did not come from the ast in the environment.
	 */
	void printNotice(ASTNode node, String msg);
	
	/**
	 * Print an error including the given arguments for use
	 * in quick-fixes. These arguments will show up in the problem
	 * arguments passed during quick-fix operation, with an ID
	 * defined by EclipseMessager.APT_QUICK_FIX_PROBLEM_ID.
	 */
	void printFixableError(SourcePosition pos, String msg, String... arguments);
	
	/**
	 * Print an warning including the given arguments for use
	 * in quick-fixes. These arguments will show up in the problem
	 * arguments passed during quick-fix operation, with an ID
	 * defined by EclipseMessager.APT_QUICK_FIX_PROBLEM_ID.
	 */
	void printFixableWarning(SourcePosition pos, String msg, String... arguments);
	
	/**
	 * Print a notice including the given arguments for use
	 * in quick-fixes. These arguments will show up in the problem
	 * arguments passed during quick-fix operation, with an ID
	 * defined by EclipseMessager.APT_QUICK_FIX_PROBLEM_ID.
	 */
	void printFixableNotice(SourcePosition pos, String msg, String... arguments);
	
	/**
	 * Print an error including the given arguments for use
	 * in quick-fixes. These arguments will show up in the problem
	 * arguments passed during quick-fix operation, with an ID
	 * defined by EclipseMessager.APT_QUICK_FIX_PROBLEM_ID.
	 */
	void printFixableError(String msg, String... arguments);
	
	/**
	 * Print an warning including the given arguments for use
	 * in quick-fixes. These arguments will show up in the problem
	 * arguments passed during quick-fix operation, with an ID
	 * defined by EclipseMessager.APT_QUICK_FIX_PROBLEM_ID.
	 */
	void printFixableWarning(String msg, String... arguments);
	
	/**
	 * Print a notice including the given arguments for use
	 * in quick-fixes. These arguments will show up in the problem
	 * arguments passed during quick-fix operation, with an ID
	 * defined by EclipseMessager.APT_QUICK_FIX_PROBLEM_ID.
	 */
	void printFixableNotice(String msg, String... arguments);
}
