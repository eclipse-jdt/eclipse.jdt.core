package org.eclipse.jdt.core;

public interface IPackageDeclaration extends IJavaElement, ISourceReference {
	/**
	 * Returns the name of the package the statement refers to.
	 * This is a handle-only method.
	 */
	String getElementName();
}
