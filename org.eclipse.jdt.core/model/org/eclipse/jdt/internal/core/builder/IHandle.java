package org.eclipse.jdt.internal.core.builder;

public interface IHandle {
	int K_JAVA_IMAGE = 1;
	int K_JAVA_PACKAGE = 2;
	int K_JAVA_TYPE = 3;
	int K_JAVA_FIELD = 4;
	int K_JAVA_METHOD = 5;
	int K_JAVA_CONSTRUCTOR = 6;

	/**
	 * Compares this handle against the specified object.
	 * Returns true if the objects are the same.  Two handles are
	 * the same if they both represent the same object according
	 * to the identity criteria described above, and they are
	 * either both non-state-specific, or are both state-specific
	 * on the same state.  
	 * This is a handle-only method.
	 * Extending interfaces provide more details.
	 *
	 * @see Object#equals
	 */
	boolean equals(Object obj);
	/**
	 * Returns this object's development context.
	 * This is a handle-only method.
	 */
	IDevelopmentContext getDevelopmentContext();
	/**
	 * Returns the state in which the object appears.  The handle
	 * must be state-specific.
	 * This is a handle-only method.
	 * 
	 * @exception StateSpecificException if this object is not state-specific.
	 */
	IState getState() throws StateSpecificException;
	/**
	 * Returns a hashcode for this handle, consistent with the identity
	 * criteria described above.
	 * This is a handle-only method.
	 *
	 * @see Object#hashCode
	 */
	int hashCode();
	/**
	 * Returns a state-specific object which refers to the same object 
	 * as this object, but restricted to the given state.  This object
	 * must be non-state-specific.	
	 * It is an error to invoke this on a state-specific object. 
	 * This is a handle-only method; the object may or may not be present
	 * in the given state.
	 *
	 * @param state the state to use.
	 * @exception StateSpecificException if this object is already state-specific.
	 */
	IHandle inState(IState s) throws StateSpecificException;
	/**
	 * Returns true if the object represented by the receiver is 'fictional'.
	 * That is, it was created by the development context for error repair 
	 * purposes, such as to fill a missing reference in the source.
	 * A fictional object is not the same as a synthetic object.
	 *
	 * @exception NotPresentException if the object is not present.
	 *
	 * @see IMember#isSynthetic
	 */
	boolean isFictional() throws NotPresentException;
	/**
	 * Returns true if the object represented by the receiver is present 
	 * in the development context, false otherwise.  If the receiver is 
	 * state-specific, checks whether it is present in this object's state.
	 * If the receiver is non-state-specific, checks whether it is present
	 * in the current state of the development context.  In the latter case,
	 * if there is no current state, <code>NotPresentException</code> is thrown.
	 */
	boolean isPresent();
	/**
	 * Returns true if this object represents an object in a specific 
	 * state, false otherwise.
	 * This is a handle-only method.
	 *
	 * @see IState
	 */
	boolean isStateSpecific();
	/**
	 * Returns a constant indicating what kind of handle this is.
	 * See the constants below.
	 */
	int kind();
	/**
	 * Returns a non-state-specific handle corresponding to this object.
	 * The object must be state-specific.
	 * This is a handle-only method.
	 *
	 * @exception StateSpecificException if this object is already non-state-specific.
	 * @see IState
	 */
	IHandle nonStateSpecific() throws StateSpecificException;
	/**
	 * Return a string describing this handle.	
	 * This is a handle-only method.
	 * Extending interfaces provide more details.
	 *
	 * @see Object#toString
	 */
	String toString();
}
