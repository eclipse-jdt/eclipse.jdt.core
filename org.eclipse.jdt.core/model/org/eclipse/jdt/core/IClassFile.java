package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * Represents an entire binary type (single <code>.class</code> file). 
 * A class file has a single child of type <code>IType</code>.
 * Class file elements need to be opened before they can be navigated.
 * If a class file cannot be parsed, its structure remains unknown. Use 
 * <code>IJavaElement.isStructureKnown</code> to determine whether this is the
 * case.
 * <p>
 * Note: <code>IClassFile</code> extends <code>ISourceReference</code>.
 * Source can be obtained for a class file iff source has been attached to this
 * class file. The source associated with a class file is the source code of
 * the compilation unit it was (nominally) generated from.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPackageFragmentRoot#attachSource
 */
 
public interface IClassFile extends IJavaElement, IParent, IOpenable, ISourceReference, ICodeAssist {
/**
 * Returns the smallest element within this class file that 
 * includes the given source position (a method, field, etc.), or
 * <code>null</code> if there is no element other than the class file
 * itself at the given position, or if the given position is not
 * within the source range of this class file.
 *
 * @param position a source position inside the class file
 * @return the innermost Java element enclosing a given source position or <code>null</code>
 *  if none (excluding the class file).
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
IJavaElement getElementAt(int position) throws JavaModelException;
/**
 * Returns the type contained in this class file.
 *
 * @return the type contained in this class file
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
IType getType() throws JavaModelException;
/**
 * Returns whether this type represents a class. This is not guaranteed to be
 * instantaneous, as it may require parsing the underlying file.
 *
 * @return <code>true</code> if the class file represents a class.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
boolean isClass() throws JavaModelException;
/**
 * Returns whether this type represents an interface. This is not guaranteed to
 * be instantaneous, as it may require parsing the underlying file. 
 *
 * @return <code>true</code> if the class file represents an interface.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
boolean isInterface() throws JavaModelException;
}
