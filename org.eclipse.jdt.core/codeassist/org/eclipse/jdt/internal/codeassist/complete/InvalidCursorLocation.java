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
	public static final String NO_COMPLETION_INSIDE_UNICODE = "No Completion Inside Unicode"/*nonNLS*/;
	public static final String NO_COMPLETION_INSIDE_COMMENT = "No Completion Inside Comment"/*nonNLS*/;     
	public static final String NO_COMPLETION_INSIDE_STRING = "No Completion Inside String"/*nonNLS*/;       
	public static final String NO_COMPLETION_INSIDE_NUMBER = "No Completion Inside Number"/*nonNLS*/;       
    
public InvalidCursorLocation(String irritant){
	this.irritant = irritant;
}
}
