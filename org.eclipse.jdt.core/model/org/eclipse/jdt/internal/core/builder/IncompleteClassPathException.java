package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Exception thrown when the build should be aborted because the project's
 * class path is incomplete/incorrect.
 */
public class IncompleteClassPathException extends RuntimeException {

protected String missingClassFile;

public IncompleteClassPathException(String missingClassFile) {
	this.missingClassFile = missingClassFile;
}
}