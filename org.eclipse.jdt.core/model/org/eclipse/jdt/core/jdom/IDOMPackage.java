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
package org.eclipse.jdt.core.jdom;
/**
 * Represents a package declaration. 
 * The corresponding syntactic unit is PackageDeclaration (JLS2 7.4). 
 * A Package has no children, and its parent is a compilation unit.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IDOMPackage extends IDOMNode {
/**
 * The <code>IDOMPackage</code> refinement of this <code>IDOMNode</code>
 * method returns the name of this package declaration, or <code>null</code>
 * if it has none. The syntax for a package name corresponds to PackageName
 * as defined by PackageDeclaration (JLS2 7.4).
 */
public String getName();
/**
 * The <code>IDOMPackage</code> refinement of this <code>IDOMNode</code>
 * method sets the name of this package declaration. The syntax for a package
 * name corresponds to PackageName as defined by PackageDeclaration (JLS2 7.4).
 * A <code>null</code> name indicates an empty package declaration; that is,
 * <code>getContents</code> returns the empty string.
 */
public void setName(String name);
}
