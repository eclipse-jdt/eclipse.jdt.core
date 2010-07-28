/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;


/**
 * Represents a package declaration in Java compilation unit.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPackageDeclaration extends IJavaElement, ISourceReference, IAnnotatable {
/**
 * Returns the name of the package the statement refers to.
 * This is a handle-only method.
 *
 * @return the name of the package the statement
 */
String getElementName();
/**
 * Returns the source range of this package declaration's name,
 * or <code>null</code> if this package declaration does not have
 * associated source code (for example, a binary type).
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource.
 * @return the source range of this package declaration's name,
 * or <code>null</code> if this package declaration does not have
 * associated source code (for example, a binary type)
 * @since 3.7
 */
ISourceRange getNameRange() throws JavaModelException;

}
