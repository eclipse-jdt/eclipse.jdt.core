package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

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
