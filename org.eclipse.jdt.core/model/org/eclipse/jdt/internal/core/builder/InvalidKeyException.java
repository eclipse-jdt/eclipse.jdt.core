package org.eclipse.jdt.internal.core.builder;

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
	 * @param	   s   the detail message.
	 */
	public InvalidKeyException(String s) {
		super(s);
	}
}
