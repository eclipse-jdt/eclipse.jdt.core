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

/**
 * Abstract base class of AST nodes that represent modifiers or annotations.
 * <pre>
 * ExtendedModifier:
 *   Modifier
 *   Annotation
 * </pre>
 * <p>
 * Note: Support for annotation metadata is an experimental language feature 
 * under discussion in JSR-175 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * @since 3.0
 */
public abstract class ExtendedModifier extends ASTNode {
	
	/**
	 * Creates a new unparented node for an extended modifier owned by the
	 * given AST. By default, no bound.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ExtendedModifier(AST ast) {
		super(ast);
	}

	/**
	 * Returns whether this extended modifier is a standard modifier.
	 * 
	 * @return <code>true</code> if this is a standard modifier,
	 *    and <code>false</code> otherwise
	 */ 
	public boolean isModifier() {
		return (this instanceof Modifier);
	}
	
	/**
	 * Returns whether this extended modifier is an annotation.
	 * 
	 * @return <code>true</code> if this is an annotation,
	 *    and <code>false</code> otherwise
	 */ 
	public boolean isAnnotation() {
		return (this instanceof Annotation);
	}
}

