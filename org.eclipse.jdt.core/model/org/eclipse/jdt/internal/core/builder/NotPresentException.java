package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * Thrown when a method is invoked on a handle whose object
 * is not present.  See <code>IHandle</code> for more details.
 * 
 * This is an unchecked exception (a subclass of RuntimeException)
 * because most of the time, code can ignore it if it is known to be
 * navigating a state (see <code>IHandle</code> for more details).  
 * In some situations however, such as creating handles, the user 
 * would have to either
 * <ol>
 * <li>catch the exception, or</li>
 * <li>test with <code>isPresent()</code> before performing the operation.</li>
 * </ol>
 *
 * @see IHandle
 */
public class NotPresentException extends RuntimeException
{


	/**
	 * Constructs a <code>NotPresentException</code> without a detail message.
	 */
	public NotPresentException() {
		super();
	}
	/**
	 * Constructs a <code>NotPresentException</code> with a detail message. 
	 *
	 * @param      s   the detail message.
	 */
	public NotPresentException(String s) {
		super(s);
	}
}
