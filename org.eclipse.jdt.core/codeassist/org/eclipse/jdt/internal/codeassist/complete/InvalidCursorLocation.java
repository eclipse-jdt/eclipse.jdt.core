package org.eclipse.jdt.internal.codeassist.complete;

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
