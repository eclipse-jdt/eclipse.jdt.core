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
 * Represents a package declaration in Java compilation unit.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IPackageDeclaration extends IJavaElement, ISourceReference {
/**
 * Returns the name of the package the statement refers to.
 * This is a handle-only method.
 */
String getElementName();
}
