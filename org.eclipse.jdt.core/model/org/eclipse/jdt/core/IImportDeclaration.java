package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * Represents an import declaration in Java compilation unit.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IImportDeclaration extends IJavaElement, ISourceReference, ISourceManipulation {
/**
 * Returns the name that has been imported. 
 * For an on-demand import, this includes the trailing <code>".*"</code>.
 * For example, for the statement <code>"import java.util.*"</code>,
 * this returns <code>"java.util.*"</code>.
 * For the statement <code>"import java.util.Hashtable"</code>,
 * this returns <code>"java.util.Hashtable"</code>.
 */
String getElementName();
/**
 * Returns whether the import is on-demand. An import is on-demand if it ends
 * with <code>".*"</code>.
 */
boolean isOnDemand();
}
