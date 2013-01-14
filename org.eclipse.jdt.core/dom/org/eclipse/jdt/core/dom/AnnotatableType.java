/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * Type node for an annotatable type.
 * <p>
 * Introduced in JLS8, type references that can be annotated are represented by 
 * AnnotatableType. For the list of types extending AnnotatableType, see {@link Type}</p>
 *
 * @since 3.9
 */
public abstract class AnnotatableType extends Type {

	/**
	 * Creates a new unparented node for an annotatable type owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 *
	 * @param ast the AST that is to own this node
	 */
	AnnotatableType(AST ast) {
		super(ast);
	}
	
	ASTNode.NodeList annotations = null;
	
	/**
	 * Returns the live ordered list of annotations for this Type node.
	 *
	 * @return the live list of annotations (element type: {@link Annotation})
	 * @exception UnsupportedOperationException if this operation is used
	 *            in a JLS2, JLS3 or JLS4 AST
	 * @since 3.9
	 */
	public List annotations() {
		if (this.annotations == null) {
			unsupportedIn2_3_4();
		}
		return this.annotations;
	}
}
