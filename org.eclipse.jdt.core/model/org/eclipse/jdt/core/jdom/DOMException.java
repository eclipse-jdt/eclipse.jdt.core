package org.eclipse.jdt.core.jdom;

public class DOMException extends RuntimeException {
/**
 * Creates a new exception with no detail message.
 */
public DOMException() {}
/**
 * Creates a new exception with the given detail message.
 *
 * @param message the deatil message
 */
public DOMException(String message) {
	super(message);
}
}
