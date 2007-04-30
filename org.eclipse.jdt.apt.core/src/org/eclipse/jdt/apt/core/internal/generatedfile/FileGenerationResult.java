/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     mkaufman@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.generatedfile;

import org.eclipse.core.resources.IFile;

/**
 * Simple container class for holding the result of file generation.<P>
 * 
 * It contains the generated file, as well as a boolean indicating if it
 * has changed since it was last seen. This is used to force compilation 
 * of the file later.
 */
public class FileGenerationResult {

	private final IFile file;
	private final boolean modified;
	
	public FileGenerationResult(final IFile file, final boolean modified) {
		this.file = file;
		this.modified = modified;
	}
	
	public IFile getFile() {
		return file;
	}
	
	public boolean isModified() {
		return modified;
	}
}
