package org.eclipse.jdt.internal.core.newbuilder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Exception thrown when there is an internal error in the image builder.
 * May wrapper another exception.
 */
public class ImageBuilderInternalException extends RuntimeException {

protected Throwable throwable;

public ImageBuilderInternalException(Throwable t) {
	throwable = t;
}

public Throwable getThrowable() {
	return throwable;
}

public void printStackTrace() {
	if (throwable != null) {
		System.err.println(this);
		System.err.println("Stack trace of embedded throwable:"); //$NON-NLS-1$
		throwable.printStackTrace();
	} else {
		super.printStackTrace();
	}
}
}