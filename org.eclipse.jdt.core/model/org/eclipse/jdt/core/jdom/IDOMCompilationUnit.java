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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.WorkingCopyOwner;

/**
 * Represents a Java compilation unit (<code>.java</code> source file). 
 * The corresponding syntactic unit is CompilationUnit (JLS2 7.3).  
 * Allowable child types for a compilation unit are <code>IDOMPackage</code>, <code>IDOMImport</code>,
 * and <code>IDOMType</code>.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IDOMCompilationUnit extends IDOMNode {
/**
 * Returns the header comment for this compilation unit. The header comment
 * appears before the first declaration in a compilation unit.
 * The syntax for a comment corresponds to Comments (JLS2 3.7), <b>including</b>
 * comment delimiters.
 *
 * @return the header comment for this compilation unit, or <code>null</code> if
 *   no header comment is present
 */
public String getHeader();
/**
 * The <code>IDOMCompilationNode</code> refinement of this <code>IDOMNode</code>
 * method returns the name of this compilation unit.
 *
 * <p>The name of a compilation unit is the name of the first top-level public type
 * defined in the compilation unit, suffixed with ".java". For example, if the first
 * top-level public type defined in this compilation unit has the name "Hanoi",
 * then name of this compilation unit is "Hanoi.java".</p>
 *
 * <p>In the absence of a public top-level type, the name of the first top-level
 * type is used. In the absence of any type, the name of the compilation unit
 * is <code>null</code>.</p>
 *
 * @return the name of this compilation unit, or <code>null</code> if none
 */
public String getName();
/**
 * Returns a handle for the compilation unit associated with this 
 * document fragment, based on the parent package fragment.
 * The owner of the returned comppilation unit is the given owner
 * if such a working copy exists, otherwise the compilation unit is a 
 * primary compilation unit..
 *
 * @param parent the parent package fragment
 * @param owner the owner of the returned compilation unit
 * @exception IllegalArgumentException if the parent element is not
 *   of a valid parent type for this node
 * @since 3.0
 */
public ICompilationUnit getCompilationUnit(IPackageFragment parent, WorkingCopyOwner owner) throws IllegalArgumentException;
/**
 * Sets the header comment for this compilation unit. The header comment
 * appears before the first declaration in a compilation unit.
 * The syntax for a comment corresponds to Comments (JLS2 3.7), <b>including</b>
 * comment delimiters.
 *
 * @param comment the header comment for this compilation unit, or <code>null</code> if
 *   indicating no header comment
 */
public void setHeader(String comment);
/**
 * The <code>IDOMCompilationNode</code> refinement of this <code>IDOMNode</code>
 * method has no effect (the name is computed from the types declared within it).
 */
public void setName(String name);
}
