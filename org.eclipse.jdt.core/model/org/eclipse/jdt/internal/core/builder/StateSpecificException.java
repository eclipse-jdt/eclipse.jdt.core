package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * Thrown when an operation is performed on a state-specific handle
 * when it should be non-state-specific, or vice-versa.
 * See <code>IHandle</code> for more details.
 *
 * This exception is indicative of design flaws in the code.
 * The offending code is confused as to whether it is on the state-specific
 * vs. non-state-specific side of the fence.
 * This is an unchecked exception (a subclass of RuntimeException) 
 * so that client code does not have to worry about catching this exception, 
 * which should never be thrown (or caught) in properly written code.
 *
 * @see IHandle
 * @see IState
 */
public class StateSpecificException extends RuntimeException
{


	/**
	 * Constructs a <code>StateSpecificException</code> without a detail message.
	 */
	public StateSpecificException() {
		super();
	}
	/**
	 * Constructs a <code>StateSpecificException</code> with a detail message. 
	 *
	 * @param      s   the detail message.
	 */
	public StateSpecificException(String s) {
		super(s);
	}
}
