package org.eclipse.jdt.core;

public interface IImportContainer
	extends IJavaElement, IParent, ISourceReference {
	/**
	 * Returns the first import declaration in this import container with the given name.
	 * This is a handle-only method. The import declaration may or may not exist.
	 */
	IImportDeclaration getImport(String name);
}
