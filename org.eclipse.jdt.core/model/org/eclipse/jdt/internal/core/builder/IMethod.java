package org.eclipse.jdt.internal.core.builder;

public interface IMethod extends IMember {


	/**
	 * Compares this Method handle against the specified object.  Returns
	 * true if the objects are the same.  Two Method handles are the same if
	 * they have the same declaring class and have the same method name
	 * and formal parameter types.
	 * See Handle.equals() for more details.
	 *
	 * @see IHandle#equals
	 * @see IHandle#hashCode
	 */
	boolean equals(Object obj);
	/**
	 * Returns an array of Type objects that represent the types of
	 * the checked exceptions thrown by the method
	 * represented by this Method object.
	 * Unchecked exceptions are not included in the result, even if
	 * they are declared in the source.
	 * Returns an array of length 0 if the method throws no checked 
	 * exceptions.
	 * The resulting Types are in no particular order.
	 *
	 * @exception NotPresentException if the method is not present.
	 */
	IType[] getExceptionTypes() throws NotPresentException;
	/**
	 * Returns an array of Type objects that represent the formal
	 * parameter types, in declaration order, of the method
	 * represented by this Method object.
	 * Returns an array of length 0 if the underlying method takes 
	 * no parameters.
	 * This is a handle-only method.
	 */
	IType[] getParameterTypes();
	/**
	 * Returns a Type object that represents the formal return type
	 * of the method represented by this Method object.
	 * 
	 * @exception NotPresentException if the method is not present.
	 */
	IType getReturnType() throws NotPresentException;
	/**
	 * A method is present if:
	 * <ul>
	 * <li>its declaring class is present, and
	 * <li>the class declares a method of the same name and parameter types
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
	 * Returns a string describing this Method handle.	The string is
	 * formatted as the fully qualified name of the declaring class,
	 * followed by a period, followed by the method name, 
	 * followed by a parenthesized, comma-separated list of the method's 
	 * formal parameter types. 
	 * For example:
	 * <pre>
	 *    java.lang.Object.equals(java.lang.Object)
	 *    java.lang.Object.wait(long,int)
	 * </pre>
	 *
	 * @see IHandle#toString
	 */
	String toString();
}
