package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * The <code>IHandle</code> defines messages common to objects 
 * in the image (for Java, these would be things such as packages, types, 
 * methods, and fields).
 * Each one of these has an identifying handle, which represents the object.
 * There can only be at most one such object in the image.
 * <p>
 * Handles come in two flavours: state-specific, and non-state-specific.
 * A state-specific handle is tied to a specific state and refers to an 
 * object in that state.  The state of a state-specific handle cannot
 * be changed.
 * A non-state-specific handle is not tied to a specific state and refers
 * to an object in whichever state is the current state of the development context.
 * The current state may change, in which case all non-state-specific handles
 * now refer to the object in the new current state.
 * Normally a client is dealing with non state-specific handles, occasionally with
 * state-specific handles, and very rarely with a combination of the two.
 * <p>
 * It is possible for the object refered to by a handle to be 
 * missing from the image (e.g. a class which was deleted).  
 * The isPresent() method can be used to determine whether the object 
 * is present in a state of the development context or not.  
 * For state-specific handles, isPresent() returns true if and only if the
 * object is present in the state the handle is tied to.
 * For non-state-specific handles, isPresent() returns true if and only if
 * the object is present in the current state of the DC.
 * <p>
 * Certain methods on handles require that the object it represents be present
 * (these are called 'non-handle-only methods'), whereas others operate only on 
 * the handles themselves and will work correctly whether or not the object 
 * is present (these are called 'handle-only methods').
 * If a non-handle-only method is invoked on a handle whose object is not present,
 * a <code>NotPresentException</code> is thrown (except for the isPresent() method).
 * <p>
 * The development context tries to ensure that any objects obtained by navigating
 * from a present object are also present.  For example, in Java, the classes 
 * returned by Package.getAllClasses() will all be present.
 * For cases where an object referenced in the source is not present in the image, 
 * the image builder either creates a <em>fictional</em> object to take its place or makes
 * it appear as if the reference never existed. 
 * These fictional objects are considered to be present (although it is not generally
 * true that if an object is fictional then it is present).
 * For example, if a class B subclasses a class A, and B is present in the image 
 * but A is not, then the image builder could invent a fictional class A.  So the
 * navigation B.getSuperclass() would result in an object that is present.
 * For this reason, NotPresentException is an unchecked RuntimeException, to allow 
 * clients to operate under the assumption that all reachable objects are present.
 * Special care must be taken when creating handles 
 * (i.e. <code>Package.getClassHandle(String)</type), since the objects 
 * referred to may not be present.  Other methods where this assumption does not
 * hold are explicitly marked (e.g. <code>Package.getReferencedPackages()</code>).
 * <p>
 * The following table defines the identity criteria for the principle kinds 
 * of Java handles.	For two handles to be considered the same, they must satisfy 
 * these criteria.	In addition, the handles must either both be non-state-specific,
 * or both be state-specific on the same state.
 * <p> 
 * <dl> 
 * <b><dt>Kind of handle</dt> <dd>Identity criteria</dd></b>
 * <dt>Image</dt>		<dd>Same development context.</dd>
 * <dt>Package</dt> 	<dd>Same development context, same package name.</dd>
 * <dt>Type</dt>		
 *		<dd>Same development context.
 *		For classes and interfaces, same type name (same fully qualified name,
 *      excluding package name) and same containing package.
 *		For array types, same component type.
 *		For primitive types, same primitive type name.</dd>
 * <dt>Field</dt>		<dd>Same development context, same declaring type, same field name.</dd>
 * <dt>Method</dt>		
 *		<dd>Same development context, same declaring type, same method name,
 *		same argument types.</dd>
 * <dt>Constructor</dt> 	
 *		<dd>Same development context, same declaring type, same argument types.</dd>
 * </dl>
 *
 * @see IImage
 * @see IPackage
 * @see IType
 * @see IField
 * @see IMethod
 * @see IConstructor
 * @see INotPresentException
 *
 * @see #equals
 */
public interface IHandle 
{
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
