package org.eclipse.jdt.core;

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
 *	if none (excluding the class file).
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource
 */
IJavaElement getElementAt(int position) throws JavaModelException;
/**
 * Returns the type contained in this class file.
 *
 * @return the type contained in this class file
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource
 */
IType getType() throws JavaModelException;
/**
 * Returns whether this type represents a class. This is not guaranteed to be
 * instantaneous, as it may require parsing the underlying file.
 *
 * @return <code>true</code> if the class file represents a class.
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource
 */
boolean isClass() throws JavaModelException;
/**
 * Returns whether this type represents an interface. This is not guaranteed to
 * be instantaneous, as it may require parsing the underlying file. 
 *
 * @return <code>true</code> if the class file represents an interface.
 *
 * @exception JavaModelException if this element does not exist or if an
 *		exception occurs while accessing its corresponding resource
 */
boolean isInterface() throws JavaModelException;
}
