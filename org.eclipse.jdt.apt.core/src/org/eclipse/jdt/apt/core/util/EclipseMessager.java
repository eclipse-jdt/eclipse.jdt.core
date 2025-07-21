/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API
 *******************************************************************************/
package org.eclipse.jdt.apt.core.util;

import com.sun.mirror.apt.Messager;
import com.sun.mirror.util.SourcePosition;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Extend the APT {@link Messager} to allow the registrating of diagnostics on ast nodes.
 */
public interface EclipseMessager extends Messager
{
	/**
	 * Problem ID for APT problems that do not have quick fixes
	 */
	public static final int APT_PROBLEM_ID = /*TODO: switch to IProblem.ExternalProblemNotFixable*/ 901;

	/**
	 * Problem ID for APT problems that have quick fixes.
	 * If an APT plugin wants to write a quick-fix for their problems,
	 * they should look for this ID.
	 *
	 * see org.eclipse.jdt.ui.text.java.IQuickAssistProcessor
	 */
	public static final int APT_QUICK_FIX_PROBLEM_ID = /* TODO: switch to IProblem.ExternalProblemFixable*/ 900;


	/**
	 * Print an error message on the given AST node.
	 * The AST node must came from the AST that is associated with the environment.
	 * @param msg the error message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 *         Also, if the node did not come from the ast in the environment.
	 */
	void printError(ASTNode node, String msg);

	/**
	 * Print a warning on the given AST node.
	 * The AST node must came from the AST that is associated with the environment.
	 * @param msg the warning message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 * 		   Also, if the node did not come from the ast in the environment.
	 */
	void printWarning(ASTNode node, String msg);

	/**
	 * Print a notice on the given AST node.
	 * The AST node must came from the AST that is associated with the environment.
	 * @param msg the warning message
	 * @throws IllegalArgumentException if <code>node</code> or <code>msg</code> is null.
	 *         Also, if the node did not come from the ast in the environment.
	 */
	void printNotice(ASTNode node, String msg);

	/**
	 * Print an error including the given arguments for use
	 * in quick-fixes. Any APT Quick Fix processors (@see IAPTQuickFixProcessor)
	 * registered with the provided pluginId and errorId will
	 * then get called if the user attempt to quick-fix that error.
	 *
	 * @param pos position of the error
	 * @param msg message to display to the user
	 * @param pluginId plugin which will provide an apt quick fix processor
	 *        for this error. Cannot be null.
	 * @param errorId a plugin-provided error code which will be meaningful
	 *        to the quick fix processor (e.g. "invalidAnnotationValue", etc.)
	 *        Cannot be null.
	 */
	void printFixableError(SourcePosition pos, String msg, String pluginId, String errorId);

	/**
	 * Print a warning including the given arguments for use
	 * in quick-fixes. Any APT Quick Fix processors (@see IAPTQuickFixProcessor)
	 * registered with the provided pluginId and errorId will
	 * then get called if the user attempt to quick-fix that warning.
	 *
	 * @param pos position of the error
	 * @param msg message to display to the user
	 * @param pluginId plugin which will provide an apt quick fix processor
	 *        for this error. Cannot be null.
	 * @param errorId a plugin-provided error code which will be meaningful
	 *        to the quick fix processor (e.g. "invalidAnnotationValue", etc.)
	 *        Cannot be null.
	 */
	void printFixableWarning(SourcePosition pos, String msg, String pluginId, String errorId);

	/**
	 * Print a notice including the given arguments for use
	 * in quick-fixes. Any APT Quick Fix processors (@see IAPTQuickFixProcessor)
	 * registered with the provided pluginId and errorId will
	 * then get called if the user attempt to quick-fix that notice.
	 *
	 * @param pos position of the error
	 * @param msg message to display to the user
	 * @param pluginId plugin which will provide an apt quick fix processor
	 *        for this error. Cannot be null.
	 * @param errorId a plugin-provided error code which will be meaningful
	 *        to the quick fix processor (e.g. "invalidAnnotationValue", etc.)
	 *        Cannot be null.
	 */
	void printFixableNotice(SourcePosition pos, String msg, String pluginId, String errorId);

	/**
	 * Print an error including the given arguments for use
	 * in quick-fixes. Any APT Quick Fix processors (@see IAPTQuickFixProcessor)
	 * registered with the provided pluginId and errorId will
	 * then get called if the user attempt to quick-fix that error.
	 *
	 * @param msg message to display to the user
	 * @param pluginId plugin which will provide an apt quick fix processor
	 *        for this error. Cannot be null.
	 * @param errorId a plugin-provided error code which will be meaningful
	 *        to the quick fix processor (e.g. "invalidAnnotationValue", etc.)
	 *        Cannot be null.
	 */
	void printFixableError(String msg, String pluginId, String errorId);

	/**
	 * Print a warning including the given arguments for use
	 * in quick-fixes. Any APT Quick Fix processors (@see IAPTQuickFixProcessor)
	 * registered with the provided pluginId and errorId will
	 * then get called if the user attempt to quick-fix that warning.
	 *
	 * @param msg message to display to the user
	 * @param pluginId plugin which will provide an apt quick fix processor
	 *        for this error. Cannot be null.
	 * @param errorId a plugin-provided error code which will be meaningful
	 *        to the quick fix processor (e.g. "invalidAnnotationValue", etc.)
	 *        Cannot be null.
	 */
	void printFixableWarning(String msg, String pluginId, String errorId);

	/**
	 * Print a notice including the given arguments for use
	 * in quick-fixes. Any APT Quick Fix processors (@see IAPTQuickFixProcessor)
	 * registered with the provided pluginId and errorId will
	 * then get called if the user attempt to quick-fix that notice.
	 *
	 * @param msg message to display to the user
	 * @param pluginId plugin which will provide an apt quick fix processor
	 *        for this error. Cannot be null.
	 * @param errorId a plugin-provided error code which will be meaningful
	 *        to the quick fix processor (e.g. "invalidAnnotationValue", etc.)
	 *        Cannot be null.
	 */
	void printFixableNotice(String msg, String pluginId, String errorId);
}
