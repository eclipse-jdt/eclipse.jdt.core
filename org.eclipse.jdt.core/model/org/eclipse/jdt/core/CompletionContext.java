/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;

/**
 * Completion context.
 * 
 * Represent the context in which the completion occurs.
 * 
 * @see CompletionRequestor#acceptContext(CompletionContext)
 * @since 3.1
 */
public final class CompletionContext extends InternalCompletionContext {
	/**
	 * Return signatures of expected types of a potential completion proposal at the completion position.
	 * 
	 * It's not mandatory to a completion proposal to respect this expectation. 
	 * 
	 * @return signatures expected types of a potential completion proposal at the completion position or
	 * <code>null</code> if there is no expected types.
	 * 
	 * @see Signature
	 */
	public char[][] getExpectedTypesSignatures() {
		return this.expectedTypesSignatures;
	}
	/**
	 * Return keys of expected types of a potential completion proposal at the completion position.
	 * 
	 * It's not mandatory to a completion proposal to respect this expectation. 
	 * 
	 * @return keys of expected types of a potential completion proposal at the completion position or
	 * <code>null</code> if there is no expected types.
	 * 
	 * @see org.eclipse.jdt.core.dom.ASTParser#createASTs(ICompilationUnit[], String[], org.eclipse.jdt.core.dom.ASTRequestor, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public char[][] getExpectedTypesKeys() {
		return this.expectedTypesKeys;
	}
}
