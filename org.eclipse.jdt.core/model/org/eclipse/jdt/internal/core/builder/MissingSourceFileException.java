/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

/**
 * Exception thrown when the build should be aborted because a source file is missing/empty.
 */
public class MissingSourceFileException extends RuntimeException {

protected String missingSourceFile;

public MissingSourceFileException(String missingSourceFile) {
	this.missingSourceFile = missingSourceFile;
}
}
