package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Exception thrown when the build should be aborted because a referenced
 * class file cannot be found.
 */
public class MissingClassFileException extends RuntimeException {

protected String missingClassFile;

public MissingClassFileException(String missingClassFile) {
	this.missingClassFile = missingClassFile;
}
}