package org.eclipse.jdt.internal.core.builder;

public class StateSpecificException extends RuntimeException {

	/**
	 * Constructs a <code>StateSpecificException</code> without a detail message.
	 */
	public StateSpecificException() {
		super();
	}

	/**
	 * Constructs a <code>StateSpecificException</code> with a detail message. 
	 *
	 * @param	   s   the detail message.
	 */
	public StateSpecificException(String s) {
		super(s);
	}

}
