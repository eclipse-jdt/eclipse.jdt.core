package org.eclipse.jdt.internal.eval;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * A <code>InstallException</code> is thrown when installing class files on a target has failed
 * for any reason.
 */
public class InstallException extends Exception {
/**
 * Constructs a <code>InstallException</code> with no detail  message.
 */
public InstallException() {
	super();
}
/**
 * Constructs a <code>InstallException</code> with the specified 
 * detail message. 
 *
 * @param   s   the detail message.
 */
public InstallException(String s) {
	super(s);
}
}
