package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Exception thrown when the build should be aborted because a source file is missing/empty.
 */
public class MissingSourceFileException extends RuntimeException {

protected String missingSourceFile;

public MissingSourceFileException(String missingSourceFile) {
	this.missingSourceFile = missingSourceFile;
}
}