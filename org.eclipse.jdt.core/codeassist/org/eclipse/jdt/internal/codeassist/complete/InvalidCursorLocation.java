package org.eclipse.jdt.internal.codeassist.complete;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.codeassist.impl.Engine;

/**
 * Thrown whenever cursor location is not inside a consistent token
 * i.e. inside a string, number, unicode, comments etc...
 */
public class InvalidCursorLocation extends RuntimeException {

	public String irritant;

	/* Possible irritants */
	public static final String NO_COMPLETION_INSIDE_UNICODE = "No Completion Inside Unicode"; //$NON-NLS-1$
	public static final String NO_COMPLETION_INSIDE_COMMENT = "No Completion Inside Comment";      //$NON-NLS-1$
	public static final String NO_COMPLETION_INSIDE_STRING = "No Completion Inside String";        //$NON-NLS-1$
	public static final String NO_COMPLETION_INSIDE_NUMBER = "No Completion Inside Number";        //$NON-NLS-1$
    
public InvalidCursorLocation(String irritant){
	this.irritant = irritant;
}
}
