package org.eclipse.jdt.internal.core.builder;

public class NotPresentException extends RuntimeException {

	/**
	 * Constructs a <code>NotPresentException</code> without a detail message.
	 */
	public NotPresentException() {
		super();
	}

	/**
	 * Constructs a <code>NotPresentException</code> with a detail message. 
	 *
	 * @param	   s   the detail message.
	 */
	public NotPresentException(String s) {
		super(s);
	}

}
