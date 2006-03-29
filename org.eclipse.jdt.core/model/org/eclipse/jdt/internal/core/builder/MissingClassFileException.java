/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

/**
 * Exception thrown when the build should be aborted because a referenced
 * class file cannot be found.
 */
public class MissingClassFileException extends RuntimeException {

	protected String missingClassFile;
	private static final long serialVersionUID = 3060418973806972616L; // backward compatible

public MissingClassFileException(String missingClassFile) {
	this.missingClassFile = missingClassFile;
}
}
