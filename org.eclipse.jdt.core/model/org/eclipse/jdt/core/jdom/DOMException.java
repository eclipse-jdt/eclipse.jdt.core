package org.eclipse.jdt.core.jdom;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.*;

/**
 * Unchecked exception thrown when an illegal manipulation of the JDOM is 
 * performed, or when an attempt is made to access/set an attribute of a
 * JDOM node that source indexes cannot be determined for (in case the source
 * was syntactically incorrect).
 */
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
