package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * Represents an import container is a child of a Java compilation unit that contains
 * all (and only) the import declarations. If a compilation unit has no import
 * declarations, no import container will be present.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IImportContainer extends IJavaElement, IParent, ISourceReference {
/**
 * Returns the first import declaration in this import container with the given name.
 * This is a handle-only method. The import declaration may or may not exist.
 */
IImportDeclaration getImport(String name);
}
