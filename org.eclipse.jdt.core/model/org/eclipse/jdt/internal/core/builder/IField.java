package org.eclipse.jdt.internal.core.builder;

public interface IField extends IMember {


	/**
	 * Compares this Field handle against the specified object.  Returns
	 * true if the objects are the same.  Two Field handles are the same if
	 * they have the same declaring class and have the same field name.
	 * See IHandle.equals() for more details.
	 *
	 * @see IHandle#equals
	 * @see IHandle#hashCode
	 */
	boolean equals(Object obj);
	/**
	 * Returns a Type object that identifies the declared type for
	 * the field represented by this Field object.
	 * 
	 * @exception NotPresentException if the field is not present.
	 */
	IType getType() throws NotPresentException;
	/**
	 * A field is present if:
	 * <ul>
	 * <li>its declaring class is present, and
	 * <li>the class declares a field of the same name
	 * </ul>
	 * See Handle.isPresent() for more details.
	 *
	 * @see IHandle#isPresent
	 * @see IMember#getDeclaringClass
	 */
	boolean isPresent();
	/**
	 * Return a string describing this Field.  The format is
	 * the fully-qualified name of the class declaring the field,
	 * followed by a period, followed by the name of the field.
	 * For example:
	 * <pre>
	 *	  java.lang.Thread.MIN_PRIORITY
	 *	  java.io.FileDescriptor.fd
	 * </pre>
	 *
	 * @see IHandle#toString
	 */
	String toString();
}
