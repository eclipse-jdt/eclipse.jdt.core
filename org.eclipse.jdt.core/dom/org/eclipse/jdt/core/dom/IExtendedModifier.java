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
 * Common interface for AST nodes that represent modifiers or
 * annotations.
 * <pre>
 * ExtendedModifier:
 *   Modifier
 *   Annotation
 * </pre>
 * @since 3.0
 */
public interface IExtendedModifier {
	
	/**
	 * Returns whether this extended modifier is a standard modifier.
	 * 
	 * @return <code>true</code> if this is a standard modifier
	 * (instance of {@link Modifier}), and <code>false</code> otherwise
	 */ 
	public boolean isModifier();
	
	/**
	 * Returns whether this extended modifier is an annotation.
	 * 
	 * @return <code>true</code> if this is an annotation
	 * (instance of a subclass of {@link Annotation}), and 
	 * <code>false</code> otherwise
	 */ 
	public boolean isAnnotation();
}

