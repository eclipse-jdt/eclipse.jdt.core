package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * Constructor provides information about a single
 * constructor for a class.
 *
 * Changes from java.lang and java.lang.reflect:
 * <ul>
 * <li>toString() changed to be a handle-only method; 
 *	 it ignores the modifiers and exceptions.</li>
 * </ul>
 *
 * @see IMember
 * @see IType
 * @see IType#getConstructorHandle
 * @see IType#getDeclaredConstructors
 */
public interface IConstructor extends IMember {


	/**
	 * Compares this Constructor handle against the specified object.
	 * Returns true if the objects are the same.  Two Constructor
	 * handles are the same if they were declared by the same class 
	 * and have the same formal parameter types.
	 * See Handle.equals() for more details.
	 *
	 * @see IHandle#equals
	 * @see IHandle#hashCode
	 */
	boolean equals(Object obj);
	/**
	 * Returns an array of Type objects that represent the types of
	 * the checked exceptions thrown by the underlying constructor
	 * represented by this Constructor object.  
	 * Unchecked exceptions are not included in the result, even if
	 * they are declared in the source.
	 * Returns an array of length 0 if the constructor throws no checked 
	 * exceptions.
	 * The resulting Types are in no particular order.
	 * 
	 * @exception NotPresentException if the constructor is not present.
	 */
	IType[] getExceptionTypes() throws NotPresentException;
	/**
	 * Returns an array of Type objects that represent the formal
	 * parameter types, in declaration order, of the constructor
	 * represented by this Constructor object.  
	 * Returns an array of length 0 if the underlying constructor 
	 * takes no parameters.
	 * This is a handle-only method.
	 */
	IType[] getParameterTypes();
	/**
	 * A constructor is present if:
	 * <ul>
	 * <li>its declaring class is present, and
	 * <li>the class declares a constructor with the same parameter types
	 * </ul>
	 * It is not necessary that the parameter types be present.
	 * See Handle.isPresent() for more details.
	 *
	 * @see #getParameterTypes
	 * @see IHandle#isPresent
	 * @see IMember#getDeclaringClass
	 */
	boolean isPresent();
	/**
	 * Return a string describing this Constructor.  The string is
	 * formatted as the fully-qualified name of the declaring class,
	 * followed by a parenthesized, comma-separated list of the
	 * constructor's formal parameter types.  For example:
	 * <pre>
	 *    java.util.Hashtable(int,float)
	 * </pre>
	 *
	 * @see IHandle#toString
	 */
	String toString();
}
