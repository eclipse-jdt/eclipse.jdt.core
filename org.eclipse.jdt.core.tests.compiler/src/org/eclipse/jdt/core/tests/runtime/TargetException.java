package org.eclipse.jdt.core.tests.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
/**
 * A TargetException is thrown when an operation on a target has failed
 * for any reason.
 */
public class TargetException extends Exception {
/**
 * Constructs a <code>TargetException</code> with no detail  message.
 */
public TargetException() {
	super();
}
/**
 * Constructs a <code>TargetException</code> with the specified 
 * detail message. 
 *
 * @param   s   the detail message.
 */
public TargetException(String s) {
	super(s);
}
}
