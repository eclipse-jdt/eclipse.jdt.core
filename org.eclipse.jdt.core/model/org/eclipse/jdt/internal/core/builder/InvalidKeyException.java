package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * Thrown when a DeltaKey is invalid.
 *
 * This exception is indicative of design flaws in the code.
 * The offending code has constructed a key which could not possibly
 * appear in a Delta.
 * This is an unchecked exception (a subclass of RuntimeException) 
 * so that client code does not have to worry about catching this exception, 
 * which should never be thrown (or caught) in properly written code.
 *
 * @see DeltaKey
 * @see IDelta#follow
 */
public class InvalidKeyException extends RuntimeException {
	/**
	 * Constructs an <code>InvalidKeyException</code> without a detail message.
	 */
	public InvalidKeyException() {
		super();
	}
	/**
	 * Constructs an <code>InvalidKeyException</code> with a detail message. 
	 *
	 * @param      s   the detail message.
	 */
	public InvalidKeyException(String s) {
		super(s);
	}
}
