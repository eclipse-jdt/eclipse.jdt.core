package org.eclipse.jdt.core.jdom;

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
