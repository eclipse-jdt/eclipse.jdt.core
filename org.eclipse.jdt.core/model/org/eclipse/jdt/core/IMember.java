package org.eclipse.jdt.core;

public interface IMember
	extends IJavaElement, ISourceReference, ISourceManipulation {
	/**
	 * Returns the class file in which this member is declared, or <code>null</code>
	 * if this member is not declared in a class file (for example, a source type).
	 * This is a handle-only method.
	 */
	IClassFile getClassFile();
	/**
	 * Returns the compilation unit in which this member is declared, or <code>null</code>
	 * if this member is not declared in a compilation unit (for example, a binary type).
	 * This is a handle-only method.
	 */
	ICompilationUnit getCompilationUnit();
	/**
	 * Returns the type in which this member is declared, or <code>null</code>
	 * if this member is not declared in a type (for example, a top-level type).
	 * This is a handle-only method.
	 */
	IType getDeclaringType();
	/**
	 * Returns the modifier flags for this member. The flags can be examined using class
	 * <code>Flags</code>.
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 *
	 * @see Flags
	 */
	int getFlags() throws JavaModelException;
	/**
	 * Returns the source range of this member's simple name,
	 * or <code>null</code> if this member does not have a name
	 * (for example, an initializer), or if this member does not have
	 * associated source code (for example, a binary type).
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 */
	ISourceRange getNameRange() throws JavaModelException;
	/**
	 * Returns whether this member is from a class file.
	 * This is a handle-only method.
	 *
	 * @return <code>true</code> if from a class file, and <code>false</code> if
	 *   from a compilation unit
	 */
	boolean isBinary();
}
