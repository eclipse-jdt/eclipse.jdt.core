package org.eclipse.jdt.core.compiler;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

/**
 * Exception thrown by a scanner when encountering lexical errors.
 */
public class InvalidInputException 
	/* temporary*/ extends org.eclipse.jdt.internal.compiler.parser.InvalidInputException
	/*extends Exception*/ {

	/**
	 * InvalidInputException constructor comment.
	 */
	public InvalidInputException() {
		super();
	}
	/**
	 * InvalidInputException constructor comment.
	 * @param s java.lang.String
	 */
	public InvalidInputException(String s) {
		super(s);
	}
}
