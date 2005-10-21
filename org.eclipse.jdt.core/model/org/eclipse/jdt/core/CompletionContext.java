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
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnJavadoc;

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
	 * Tell user whether completion takes place in a javadoc comment or not.
	 * 
	 * @return boolean true if completion takes place in a javadoc comment, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadoc() {
		return this.javadoc != 0;
	}

	/**
	 * Tell user whether completion takes place in text area of a javadoc comment or not.
	 * 
	 * @return boolean true if completion takes place in a text area of a javadoc comment, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadocText() {
		return (this.javadoc & CompletionOnJavadoc.TEXT) != 0;
	}

	/**
	 * Tell user whether completion takes place in a formal reference of a javadoc tag or not.
	 * Tags with formal reference are:
	 * <ul>
	 * 	<li>@see</li>
	 * 	<li>@throws</li>
	 * 	<li>@exception</li>
	 * 	<li>{@link Object}</li>
	 * 	<li>{@linkplain Object}</li>
	 * 	<li>{@value} when compiler compliance is set at leats to 1.5</li>
	 * </ul>
	 * 
	 * @return boolean true if completion takes place in formal reference of a javadoc tag, false otherwise.
	 * @since 3.2
	 */
	public boolean isInJavadocFormalReference() {
		return (this.javadoc & CompletionOnJavadoc.FORMAL_REFERENCE) != 0;
	}

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
