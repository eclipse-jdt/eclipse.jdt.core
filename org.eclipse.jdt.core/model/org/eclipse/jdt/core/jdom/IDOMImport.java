package org.eclipse.jdt.core.jdom;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * Represents an import declaration.
 * The corresponding syntactic unit is ImportDeclaration (JLS2 7.5).
 * An import has no children and its parent is a compilation unit.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IDOMImport extends IDOMNode {
/**
 * The <code>IDOMImport</code> refinement of this <code>IDOMNode</code>
 * method returns the name of this import. The syntax for an import name 
 * corresponds to a fully qualified type name, or to an on-demand package name
 * as defined by ImportDeclaration (JLS2 7.5).
 */
public String getName();
/**
 * Returns whether this import declaration ends with <code>".*"</code>.
 *
 * @return <code>true</code> if this in an on-demand import
 */
public boolean isOnDemand();
/**
 * The <code>IDOMImport</code> refinement of this <code>IDOMNode</code>
 * method sets the name of this import. The syntax for an import name 
 * corresponds to a fully qualified type name, or to an on-demand package name
 * as defined by ImportDeclaration (JLS2 7.5).
 *
 * @exception IllegalArgumentException if <code>null</code> is specified
 */
public void setName(String name);
}
