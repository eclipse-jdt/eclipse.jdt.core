package org.eclipse.jdt.core.jdom;

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
