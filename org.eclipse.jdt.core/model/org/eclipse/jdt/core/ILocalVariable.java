/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * Represents a local variable declared in a method or an initializer.
 * TODO (jreome) specify how it is created (not a child of IMethod)
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * @since 3.0
 */
public interface ILocalVariable extends IJavaElement {
	/**
	 * Returns the name of this local variable.
	 * @return the name of this local variable.
	 */
	String getElementName();
	/**
	 * Returns the source range of this local variable's name.
	 *
	 * @return the source range of this local variable's name
	 */
	ISourceRange getNameRange();
}
