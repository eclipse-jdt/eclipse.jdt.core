package org.eclipse.jdt.internal.core.builder.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * Exception thrown when there is an internal error in the image builder.
 * May wrapper another exception.
 */
public class ImageBuilderInternalException extends RuntimeException {
	protected Throwable fThrowable;
/**
 * Creates the exception with no error message.
 */
public ImageBuilderInternalException() {
}
/**
 * Creates the exception with an error message.
 */
public ImageBuilderInternalException(String s) {
	super(s);
}
/**
 * Creates the exception, wrappering another throwable.
 */
public ImageBuilderInternalException(Throwable t) {
	fThrowable = t;
}
/**
 * Returns the throwable which this wraps, or null if not applicable.
 */
public Throwable getThrowable() {
	return fThrowable;
}
	/**
	 * Prints the exception to System.err.
	 */
	public void printStackTrace() {
		if (fThrowable != null) {
			System.err.println(this);
			System.err.println("Stack trace of embedded throwable:"/*nonNLS*/);
			fThrowable.printStackTrace();
		} else {
			super.printStackTrace();
		}
	}
}
