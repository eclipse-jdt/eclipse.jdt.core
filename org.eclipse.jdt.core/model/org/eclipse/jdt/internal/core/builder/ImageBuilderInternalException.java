package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;

/**
 * Exception thrown when there is an internal error in the image builder.
 * May wrapper another exception.
 */
public class ImageBuilderInternalException extends RuntimeException {

protected CoreException coreException;

public ImageBuilderInternalException(CoreException e) {
	this.coreException = e;
}

public CoreException getThrowable() {
	return coreException;
}

public void printStackTrace() {
	if (coreException != null) {
		System.err.println(this);
		System.err.println("Stack trace of embedded core exception:"); //$NON-NLS-1$
		coreException.printStackTrace();
	} else {
		super.printStackTrace();
	}
}
}