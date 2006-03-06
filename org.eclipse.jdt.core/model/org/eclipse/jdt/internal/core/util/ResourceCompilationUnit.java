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
package org.eclipse.jdt.internal.core.util;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;

/**
 * An ICompilationUnit that retrieves its contents using an IFile
 */
public class ResourceCompilationUnit extends CompilationUnit {
	
	private IFile file;
	private URI location;
	
	public ResourceCompilationUnit(IFile file, URI location) {
		super(null/*no contents*/, location == null ? file.getFullPath().toString() : location.getSchemeSpecificPart(), null/*encoding is used only when retrieving the contents*/);
		this.file = file;
		this.location = location;
	}

	public char[] getContents() {
		if (this.contents != null)
			return this.contents;   // answer the cached source
	
		// otherwise retrieve it
		try {
			String encoding = file.getCharset();
			return Util.getResourceContentsAsCharArray(this.file, encoding, this.location);
		} catch (CoreException e) {
			return CharOperation.NO_CHAR;
		}
	}
}
